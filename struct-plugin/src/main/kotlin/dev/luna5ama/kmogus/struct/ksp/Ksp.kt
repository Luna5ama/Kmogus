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
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo
import dev.luna5ama.kmogus.MemoryArray
import dev.luna5ama.kmogus.MemoryPointer
import dev.luna5ama.kmogus.MemoryStack
import dev.luna5ama.kmogus.Span
import dev.luna5ama.kmogus.struct.Field
import dev.luna5ama.kmogus.struct.Padding
import dev.luna5ama.kmogus.struct.Struct
import sun.misc.Unsafe
import kotlin.reflect.KMutableProperty1

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
        val fieldAnnotations = mutableMapOf<String, Field>()

        for (property in list) {
            val name = property.simpleName.asString()
            val type = property.type.resolve().toClassName()
            val typeName = type.simpleName

            property.getAnnotationsByType(Padding::class).firstOrNull()?.let {
                val size = it.size
                offset += size
            }

            val fieldSize = when (typeName) {
                "Byte" -> 1L
                "Short" -> 2L
                "Int" -> 4L
                "Long" -> 8L
                "Float" -> 4L
                "Double" -> 8L
                "Char" -> 2L
                "Boolean" -> 1L
                else -> throw Exception("Unsupported type: $typeName")
            }

            val pOffset = plusOffset(offset)
            val annotation = Field(offset, fieldSize)

            fieldTypes[name] = type
            fieldAnnotations[name] = annotation
            propertyList.add(
                PropertySpec.builder(name, type)
                    .addAnnotation(AnnotationSpec.get(annotation))
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
        val structSize = (offset + sizeAlignment - 1) / sizeAlignment * sizeAlignment
        val selfType = ClassName(packageName, simpleName)

        FileSpec.builder(packageName, simpleName)
            .addType(
                TypeSpec.valueClassBuilder(simpleName)
                    .addAnnotation(AnnotationSpec.get(Struct(sizeAlignment, fieldAlignment), true))
                    .addAnnotation(JvmInline::class)
                    .addConstructor()
                    .addOperatorFunctions(selfType)
                    .addProperties(propertyList)
                    .addCompanion(selfType, structSize, fieldTypes)
                    .build()
            )
            .addHelpers(selfType, structSize, fieldTypes, fieldAnnotations)
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
                .addParameter("array", MemoryArray::class)
                .callThisConstructor("array.address + array.offset")
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

    private fun TypeSpec.Builder.addCompanion(
        selfType: ClassName,
        structSize: Long,
        fieldTypes: Map<String, ClassName>
    ) = addType(
        TypeSpec.companionObjectBuilder()
            .addProperty(
                PropertySpec.builder("size", Long::class, KModifier.CONST)
                    .initializer("${structSize}L")
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
                    .addParameter("pointer", MemoryPointer::class)
                    .addParameters(
                        fieldTypes.map { (name, type) ->
                            ParameterSpec.builder(name, type).build()
                        }
                    )
                    .returns(selfType)
                    .addCode(
                        CodeBlock.builder()
                            .addStatement("val v = ${selfType.simpleName}(pointer.address)")
                            .apply {
                                fieldTypes.forEach { (name, _) ->
                                    addStatement("v.$name = $name")
                                }
                            }
                            .add("return v")
                            .build()
                    )
                    .build()
            ).addFunction(
                FunSpec.builder("invoke")
                    .addAnnotation(JvmStatic::class)
                    .addModifiers(KModifier.OPERATOR)
                    .addParameter("array", MemoryArray::class)
                    .addParameters(
                        fieldTypes.map { (name, type) ->
                            ParameterSpec.builder(name, type).build()
                        }
                    )
                    .returns(selfType)
                    .addCode(
                        CodeBlock.builder()
                            .addStatement("val v = ${selfType.simpleName}(array.address + array.offset)")
                            .apply {
                                fieldTypes.forEach { (name, _) ->
                                    addStatement("v.$name = $name")
                                }
                            }
                            .add("return v")
                            .build()
                    )
                    .build()
            ).addFunction(
                FunSpec.builder("invoke")
                    .addAnnotation(JvmStatic::class)
                    .addModifiers(KModifier.OPERATOR)
                    .addParameter("span", Span::class)
                    .returns(selfType)
                    .addStatement("return ${selfType.simpleName}(span.address)")
                    .build()
            ).addFunction(
                FunSpec.builder("invoke")
                    .addAnnotation(JvmStatic::class)
                    .addModifiers(KModifier.OPERATOR)
                    .addParameter("span", Span::class)
                    .addParameters(
                        fieldTypes.map { (name, type) ->
                            ParameterSpec.builder(name, type).build()
                        }
                    )
                    .returns(selfType)
                    .addCode(
                        CodeBlock.builder()
                            .addStatement("val v = ${selfType.simpleName}(span.address)")
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

    private fun FileSpec.Builder.addHelpers(
        selfType: ClassName,
        structSize: Long,
        fieldTypes: Map<String, ClassName>,
        fieldAnnotations: MutableMap<String, Field>
    ) =
        addFunction(
            FunSpec.builder(selfType.simpleName)
                .receiver(MemoryStack::class)
                .returns(selfType)
                .addStatement("return ${selfType.simpleName}(calloc($structSize))")
                .build()
        ).addFunction(
            FunSpec.builder(selfType.simpleName)
                .receiver(MemoryStack::class)
                .addParameters(
                    fieldTypes.map { (name, type) ->
                        ParameterSpec.builder(name, type).build()
                    }
                )
                .returns(selfType)
                .addStatement("return ${selfType.simpleName}(malloc($structSize), ${fieldTypes.keys.joinToString()})")
                .build()
        ).addFunction(
            FunSpec.builder("sizeof")
                .addParameter("dummy", ClassName(selfType.packageName, selfType.simpleName, "Companion"))
                .returns(Long::class)
                .addStatement("return ${structSize}L")
                .build()
        ).addFunction(
            FunSpec.builder("sizeof")
                .returns(Long::class)
                .addParameter("f", KMutableProperty1::class.asClassName().parameterizedBy(selfType, STAR))
                .beginControlFlow("return when (f)")
                .apply {
                    fieldAnnotations.forEach { (name, field) ->
                        addStatement("${selfType.simpleName}::${name} -> ${field.size}L")
                    }
                }
                .addStatement("else -> throw IllegalArgumentException(\"Unknown field \$f\")")
                .endControlFlow()
                .build()
        ).addFunction(
            FunSpec.builder("offsetof")
                .returns(Long::class)
                .addParameter("f", KMutableProperty1::class.asClassName().parameterizedBy(selfType, STAR))
                .beginControlFlow("return when (f)")
                .apply {
                    fieldAnnotations.forEach { (name, field) ->
                        addStatement("${selfType.simpleName}::${name} -> ${field.offset}L")
                    }
                }
                .addStatement("else -> throw IllegalArgumentException(\"Unknown field \$f\")")
                .endControlFlow()
                .build()
        )

    private fun plusOffset(offset: Long): String {
        return if (offset == 0L) "" else " + ${offset}L"
    }
}