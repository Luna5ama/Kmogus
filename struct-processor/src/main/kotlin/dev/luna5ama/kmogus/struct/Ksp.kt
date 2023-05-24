package dev.luna5ama.kmogus.struct

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
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

class Provider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        environment.options
        return Processor(environment)
    }
}

class Processor(private val environment: SymbolProcessorEnvironment) : SymbolProcessor {
    @OptIn(KspExperimental::class)
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val srcs = resolver.getNewFiles()
            .filter { it.isAnnotationPresent(StructSource::class) }
            .flatMap { it.declarations }
            .filterIsInstance<KSClassDeclaration>()
            .filter { it.isAnnotationPresent(Struct::class) }
            .toList()

        srcs.forEach {
            genStruct(it)
        }

        return emptyList()
    }

    @OptIn(KspExperimental::class, DelicateKotlinPoetApi::class)
    private fun genStruct(clazz: KSClassDeclaration) {
        val packageName = clazz.containingFile!!.getAnnotationsByType(StructSource::class).first().pkg
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
                            .addStatement("return UNSAFE.get${typeName}(pointer$pOffset)")
                            .build()
                    )
                    .setter(
                        FunSpec.setterBuilder()
                            .addParameter("value", type)
                            .addStatement("UNSAFE.put${typeName}(pointer$pOffset, value)")
                            .build()
                    )
                    .build()
            )

            offset += fieldSize
        }

        val sizeAlignment = clazz.getAnnotationsByType(Struct::class).first().sizeAlignment
        val size = (offset + sizeAlignment - 1) / sizeAlignment * sizeAlignment
        val selfType = ClassName(packageName, simpleName)

        FileSpec.builder(packageName, simpleName)
            .addImport("dev.luna5ama.struct4k", "UNSAFE")
            .addType(
                TypeSpec.valueClassBuilder(simpleName)
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
                .addParameter("pointer", Long::class)
                .build()
        )

    private fun TypeSpec.Builder.addOperatorFunctions(selfType: ClassName) =
        addProperty(
            PropertySpec.builder("pointer", Long::class)
                .initializer("pointer")
                .build()
        )
            .addFunction(
                FunSpec.builder("inc")
                    .addModifiers(KModifier.OPERATOR)
                    .addStatement("return ${selfType.simpleName}(pointer + size)")
                    .build()
            )
            .addFunction(
                FunSpec.builder("dec")
                    .addModifiers(KModifier.OPERATOR)
                    .addStatement("return ${selfType.simpleName}(pointer - size)")
                    .build()
            )
            .addFunction(
                FunSpec.builder("get")
                    .addModifiers(KModifier.OPERATOR)
                    .addModifiers()
                    .addParameter("index", Int::class)
                    .addStatement("return ${selfType.simpleName}(pointer + index.toLong() * size)")
                    .build()
            )
            .addFunction(
                FunSpec.builder("plus")
                    .addModifiers(KModifier.OPERATOR)
                    .addParameter("offset", Long::class)
                    .addStatement("return ${selfType.simpleName}(pointer + offset)")
                    .build()
            )
            .addFunction(
                FunSpec.builder("minus")
                    .addModifiers(KModifier.OPERATOR)
                    .addParameter("offset", Long::class)
                    .addStatement("return ${selfType.simpleName}(pointer - offset)")
                    .build()
            )
            .addFunction(
                FunSpec.builder("copyTo")
                    .addParameter("dest", selfType)
                    .addStatement("UNSAFE.copyMemory(pointer, dest.pointer, size)")
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
                .addFunction(
                    FunSpec.builder("invoke")
                        .addModifiers(KModifier.OPERATOR)
                        .addParameter("pointer", Long::class)
                        .addParameters(
                            fieldTypes.map { (name, type) ->
                                ParameterSpec.builder(name, type).build()
                            }
                        )
                        .returns(selfType)
                        .addCode(
                            CodeBlock.builder()
                                .addStatement("val v = ${selfType.simpleName}(pointer)")
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