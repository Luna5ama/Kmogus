package dev.luna5ama.kmogus.struct.ksp

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.isAbstract
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.DelicateKotlinPoetApi
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo
import dev.luna5ama.kmogus.MemoryArray
import dev.luna5ama.kmogus.MemoryPointer
import dev.luna5ama.kmogus.struct.Field
import dev.luna5ama.kmogus.struct.Padding
import dev.luna5ama.kmogus.struct.Struct
import sun.misc.Unsafe

class KmogusStructProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return KmogusStructProcessor(environment)
    }
}

class KmogusStructProcessor(private val environment: SymbolProcessorEnvironment) : SymbolProcessor {

    @OptIn(KspExperimental::class)
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val srcs = resolver.getNewFiles()
            .flatMap { it.declarations }
            .filterIsInstance<KSClassDeclaration>()
            .filter { it.isAbstract() }
            .filter { it.isAnnotationPresent(Struct::class) }
            .toList()

        srcs.forEach {
            genStruct(it)
        }

        return emptyList()
    }

    @OptIn(KspExperimental::class, DelicateKotlinPoetApi::class)
    private fun genStruct(clazz: KSClassDeclaration) {
        val packageName = clazz.packageName.asString()
        val simpleName = clazz.simpleName.asString()

        var offset = 0L

        val list = clazz.getAllProperties().toList()
        val fieldTypes = mutableMapOf<String, ClassName>()
        val propertyList = mutableListOf<PropertySpec>()

        for (property in list) {
            val name = property.simpleName.asString()
            val type = property.type.resolve().toClassName()
            val typeName = type.simpleName

            property.getAnnotationsByType(Padding::class).firstOrNull()?.let {
                val size = it.size
                offset += size
            }

            val fieldSize = when (typeName) {
                "Byte" -> 1
                "Short" -> 2
                "Int" -> 4
                "Long" -> 8
                "Float" -> 4
                "Double" -> 8
                "Char" -> 2
                "Boolean" -> 1
                else -> throw Exception("Unsupported type: $typeName")
            }

            val pOffset = plusOffset(offset)

            fieldTypes[name] = type
            propertyList.add(
                PropertySpec.builder(name, type)
                    .addAnnotation(
                        AnnotationSpec.builder(Field::class.java)
                            .addMember("offset = $offset")
                            .addMember("size = $fieldSize")
                            .build()
                    )
                    .mutable()
                    .getter(
                        FunSpec.getterBuilder()
                            .addStatement("return UNSAFE.get${typeName}(address$pOffset)")
                            .build()
                    )
                    .setter(
                        FunSpec.setterBuilder()
                            .addParameter("value", type)
                            .addStatement("UNSAFE.put${typeName}(address$pOffset, value)")
                            .build()
                    )
                    .build()
            )

            offset += fieldSize
        }

        val structAnnotation = clazz.getAnnotationsByType(Struct::class).first()
        val sizeAlignment = structAnnotation.sizeAlignment
        val fieldAlignment = structAnnotation.fieldAlignment
        val size = (offset + sizeAlignment - 1) / sizeAlignment * sizeAlignment
        val selfType = ClassName(packageName, simpleName)

        FileSpec.builder(packageName, simpleName)
            .addType(
                TypeSpec.valueClassBuilder(simpleName)
                    .addAnnotation(AnnotationSpec.get(Struct(sizeAlignment, fieldAlignment), true))
                    .addAnnotation(JvmInline::class)
                    .addConstructor()
                    .addOperatorFunctions(selfType)
                    .addProperties(propertyList)
                    .addCompanion(selfType, fieldTypes, size)
                    .build()
            )
            .indent("    ")
            .build()
            .writeTo(environment.codeGenerator, Dependencies(true))
    }

    private fun TypeSpec.Builder.addConstructor() =
        primaryConstructor(
            FunSpec.constructorBuilder()
                .addModifiers(KModifier.PRIVATE)
                .addParameter("address", Long::class)
                .build()
        ).addFunction(
            FunSpec.constructorBuilder()
                .addParameter("pointer", MemoryPointer::class)
                .callThisConstructor("pointer.address")
                .build()
        ).addFunction(
            FunSpec.constructorBuilder()
                .addParameter("pointer", MemoryPointer::class)
                .addParameter("offset", Long::class)
                .callThisConstructor("pointer.address + offset")
                .build()
        ).addFunction(
            FunSpec.constructorBuilder()
                .addParameter("array", MemoryArray::class)
                .callThisConstructor("array.address + array.offset")
                .build()
        ).addFunction(
            FunSpec.constructorBuilder()
                .addParameter("array", MemoryArray::class)
                .addParameter("offset", Long::class)
                .callThisConstructor("array.address + array.offset + offset")
                .build()
        )

    private fun TypeSpec.Builder.addOperatorFunctions(selfType: ClassName) =
        addProperty(
            PropertySpec.builder("address", Long::class)
                .initializer("address")
                .build()
        ).addFunction(
            FunSpec.builder("inc")
                .addModifiers(KModifier.OPERATOR)
                .returns(selfType)
                .addStatement("return ${selfType.simpleName}(address + size)")
                .build()
        ).addFunction(
            FunSpec.builder("dec")
                .addModifiers(KModifier.OPERATOR)
                .returns(selfType)
                .addStatement("return ${selfType.simpleName}(address - size)")
                .build()
        ).addFunction(
            FunSpec.builder("get")
                .addModifiers(KModifier.OPERATOR)
                .returns(selfType)
                .addParameter("index", Int::class)
                .addStatement("return ${selfType.simpleName}(address + index.toLong() * size)")
                .build()
        ).addFunction(
            FunSpec.builder("plus")
                .addModifiers(KModifier.OPERATOR)
                .returns(selfType)
                .addParameter("offset", Long::class)
                .addStatement("return ${selfType.simpleName}(address + offset)")
                .build()
        ).addFunction(
            FunSpec.builder("minus")
                .addModifiers(KModifier.OPERATOR)
                .returns(selfType)
                .addParameter("offset", Long::class)
                .addStatement("return ${selfType.simpleName}(address - offset)")
                .build()
        ).addFunction(
            FunSpec.builder("copyTo")
                .addParameter("dest", selfType)
                .addStatement("UNSAFE.copyMemory(address, dest.address, size)")
                .build()
        )

    private fun TypeSpec.Builder.addCompanion(selfType: ClassName, fieldTypes: Map<String, ClassName>, size: Long) =
        addType(
            TypeSpec.companionObjectBuilder()
                .addProperty(
                    PropertySpec.builder("size", Long::class, KModifier.CONST)
                        .initializer("${size}L")
                        .build()
                )
                .addProperty(
                    PropertySpec.builder("UNSAFE", Unsafe::class, KModifier.PRIVATE)
                        .addAnnotation(JvmStatic::class)
                        .initializer(
                            CodeBlock.builder()
                                .beginControlFlow("run")
                                .addStatement("val field = Unsafe::class.java.getDeclaredField(\"theUnsafe\")")
                                .addStatement("field.isAccessible = true")
                                .addStatement("field.get(null) as Unsafe")
                                .endControlFlow()
                                .build()
                        )
                        .build()
                )
                .addFunction(
                    FunSpec.builder("invoke")
                        .addAnnotation(JvmStatic::class)
                        .addModifiers(KModifier.OPERATOR)
                        .addParameter("address", Long::class)
                        .addParameters(
                            fieldTypes.map { (name, type) ->
                                ParameterSpec.builder(name, type).build()
                            }
                        )
                        .returns(selfType)
                        .addCode(
                            CodeBlock.builder()
                                .addStatement("val v = ${selfType.simpleName}(address)")
                                .apply {
                                    fieldTypes.forEach { (name, _) ->
                                        addStatement("v.$name = $name")
                                    }
                                }
                                .add("return v")
                                .build()
                        )
                        .build()
                )
                .build()
        )

    private fun plusOffset(offset: Long): String {
        return if (offset == 0L) "" else " + ${offset}L"
    }
}