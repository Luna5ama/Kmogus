package dev.luna5ama.kmogus.struct.ksp

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.ClassKind
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
import dev.luna5ama.kmogus.MemoryStack
import dev.luna5ama.kmogus.MutablePointerContainer
import dev.luna5ama.kmogus.Pointer
import dev.luna5ama.kmogus.PointerContainer
import dev.luna5ama.kmogus.struct.Field
import dev.luna5ama.kmogus.struct.Padding
import dev.luna5ama.kmogus.struct.Struct
import sun.misc.Unsafe
import kotlin.reflect.KMutableProperty1

class KmogusStructProcessor(private val environment: SymbolProcessorEnvironment) : SymbolProcessor {
    @OptIn(KspExperimental::class, ExperimentalStdlibApi::class)
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val validTypes = mutableSetOf<KSClassDeclaration>()

        val analyzeResults = mutableMapOf<KSClassDeclaration, StructInfo>()
        val analyzeStruct = DeepRecursiveFunction<KSClassDeclaration, StructInfo> { clazz ->
            analyzeResults.getOrPut(clazz) {
                val packageName = clazz.packageName.asString()
                val simpleName = clazz.simpleName.asString()

                val structAnnotation = clazz.getAnnotationsByType(Struct::class).first()
                val sizeAlignment = structAnnotation.sizeAlignment
                val fieldAlignment = structAnnotation.fieldAlignment

                var offset = 0L
                val fields = mutableListOf<FieldInfo>()

                for (property in clazz.getAllProperties()) {
                    val name = property.simpleName.asString()
                    val type = property.type.resolve()
                    val className = type.toClassName()
                    val typeName = className.simpleName

                    val paddingAnnotation = property.getAnnotationsByType(Padding::class).firstOrNull()
                    paddingAnnotation?.let {
                        val size = it.size
                        offset += size
                    }

                    var isPrimitive = true

                    val fieldSize = when (typeName) {
                        "Byte" -> 1L
                        "Short" -> 2L
                        "Int" -> 4L
                        "Long" -> 8L
                        "Float" -> 4L
                        "Double" -> 8L
                        "Char" -> 2L
                        "Boolean" -> 1L
                        else -> {
                            isPrimitive = false
                            val declaration = type.declaration
                            if (declaration in validTypes && declaration is KSClassDeclaration) {
                                callRecursive(declaration).size
                            } else {
                                throw Exception("Unsupported type: $typeName")
                            }
                        }
                    }

                    if (fieldAlignment) {
                        offset = (offset + fieldSize - 1) / fieldSize * fieldSize
                    }

                    fields.add(
                        FieldInfo(
                            name,
                            className,
                            isPrimitive,
                            offset,
                            fieldSize,
                            paddingAnnotation
                        )
                    )

                    offset += fieldSize
                }

                val structSize = (offset + sizeAlignment - 1) / sizeAlignment * sizeAlignment

                StructInfo(
                    simpleName,
                    packageName,
                    Struct(sizeAlignment, fieldAlignment, structSize),
                    fields
                )
            }
        }

        resolver.getSymbolsWithAnnotation("dev.luna5ama.kmogus.struct.Struct")
            .filterIsInstance<KSClassDeclaration>()
            .filter { it.classKind == ClassKind.INTERFACE }
            .toCollection(validTypes)

        validTypes.asSequence().map {
            analyzeStruct.invoke(it)
        }.forEach {
            genStruct(it)
        }

        return emptyList()
    }

    @OptIn(DelicateKotlinPoetApi::class)
    private fun genStruct(struct: StructInfo) {
        fun TypeSpec.Builder.addConstructor() =
            primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter("address", Long::class)
                    .build()
            ).addFunction(
                FunSpec.constructorBuilder()
                    .addParameter("container", PointerContainer::class)
                    .callThisConstructor("container.pointer.address")
                    .build()
            ).addFunction(
                FunSpec.constructorBuilder()
                    .addParameter("container", MutablePointerContainer::class)
                    .callThisConstructor("container.pointer.address")
                    .build()
            )

        fun TypeSpec.Builder.addBasics() = addProperty(
            PropertySpec.builder("address", Long::class)
                .initializer("address")
                .build()
        ).addFunction(
            FunSpec.builder("asPointer")
                .returns(Pointer::class)
                .addStatement("return Pointer(address)")
                .build()
        )

        fun TypeSpec.Builder.addOperatorFunctions() =
            addFunction(
                FunSpec.builder("inc")
                    .addModifiers(KModifier.OPERATOR)
                    .returns(struct.type)
                    .addStatement("return ${struct.name}(address + size)")
                    .build()
            ).addFunction(
                FunSpec.builder("dec")
                    .addModifiers(KModifier.OPERATOR)
                    .returns(struct.type)
                    .addStatement("return ${struct.name}(address - size)")
                    .build()
            ).addFunction(
                FunSpec.builder("get")
                    .addModifiers(KModifier.OPERATOR)
                    .returns(struct.type)
                    .addParameter("index", Int::class)
                    .addStatement("return ${struct.name}(address + index.toLong() * size)")
                    .build()
            ).addFunction(
                FunSpec.builder("plus")
                    .addModifiers(KModifier.OPERATOR)
                    .returns(struct.type)
                    .addParameter("offset", Long::class)
                    .addStatement("return ${struct.name}(address + offset)")
                    .build()
            ).addFunction(
                FunSpec.builder("minus")
                    .addModifiers(KModifier.OPERATOR)
                    .returns(struct.type)
                    .addParameter("offset", Long::class)
                    .addStatement("return ${struct.name}(address - offset)")
                    .build()
            ).addFunction(
                FunSpec.builder("copyTo")
                    .addParameter("dest", struct.type)
                    .addStatement("UNSAFE.copyMemory(address, dest.address, size)")
                    .build()
            )

        fun FunSpec.Builder.addFieldAsParameters(): FunSpec.Builder = addParameters(
            struct.fields.map {
                ParameterSpec.builder(it.name, it.type).build()
            }
        )

        fun CodeBlock.Builder.addFieldInitializers(): CodeBlock.Builder {
            struct.fields.forEach {
                addStatement("v.${it.name} = ${it.name}")
            }
            return this
        }

        fun TypeSpec.Builder.addFieldProperties() = addProperties(
            struct.fields.map {
                val typeName = it.type.simpleName
                val pOffset = if (it.offset == 0L) "" else " + ${it.offset}L"

                PropertySpec.builder(it.name, it.type)
                    .addAnnotation(AnnotationSpec.get(it.fieldAnnotation))
                    .mutable()
                    .apply {
                        if (it.isPrimitive) {
                            getter(
                                FunSpec.getterBuilder()
                                    .addStatement("return UNSAFE.get${typeName}(address$pOffset)")
                                    .build()
                            ).setter(
                                FunSpec.setterBuilder()
                                    .addParameter("value", it.type)
                                    .addStatement("UNSAFE.put${typeName}(address$pOffset, value)")
                                    .build()
                            )
                        } else {
                            getter(
                                FunSpec.getterBuilder()
                                    .addStatement("return ${it.type}(address${pOffset})")
                                    .build()
                            ).setter(
                                FunSpec.setterBuilder()
                                    .addParameter("value", it.type)
                                    .addStatement("UNSAFE.copyMemory(value.address, address${pOffset}, ${it.type}.size)")
                                    .build()
                            )
                        }
                    }
                    .build()
            }
        )

        fun TypeSpec.Builder.addCompanion() = addType(
            TypeSpec.companionObjectBuilder()
                .addProperty(
                    PropertySpec.builder("size", Long::class, KModifier.CONST)
                        .initializer("${struct.size}L")
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
                        .addParameter("container", PointerContainer::class)
                        .addFieldAsParameters()
                        .returns(struct.type)
                        .addCode(
                            CodeBlock.builder()
                                .addStatement("val v = ${struct.name}(container)")
                                .apply {
                                    struct.fields.forEach {
                                        addStatement("v.${it.name} = ${it.name}")
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
                        .addParameter("container", MutablePointerContainer::class)
                        .addParameters(
                            struct.fields.map {
                                ParameterSpec.builder(it.name, it.type).build()
                            }
                        )
                        .returns(struct.type)
                        .addCode(
                            CodeBlock.builder()
                                .addStatement("val v = ${struct.name}(container)")
                                .addFieldInitializers()
                                .add("return v")
                                .build()
                        )
                        .build()
                ).addFunction(
                    FunSpec.builder("invoke")
                        .addAnnotation(JvmStatic::class)
                        .addModifiers(KModifier.OPERATOR)
                        .addParameter("pointer", Pointer::class)
                        .returns(struct.type)
                        .addStatement("return ${struct.name}(pointer.address)")
                        .build()
                ).addFunction(
                    FunSpec.builder("invoke")
                        .addAnnotation(JvmStatic::class)
                        .addModifiers(KModifier.OPERATOR)
                        .addParameter("pointer", Pointer::class)
                        .addFieldAsParameters()
                        .returns(struct.type)
                        .addCode(
                            CodeBlock.builder()
                                .addStatement("val v = ${struct.name}(pointer.address)")
                                .addFieldInitializers()
                                .add("return v")
                                .build()
                        )
                        .build()
                )
                .build()
        )

        fun FileSpec.Builder.addHelpers() =
            addFunction(
                FunSpec.builder(struct.name)
                    .receiver(MemoryStack::class)
                    .returns(struct.type)
                    .addStatement("return ${struct.name}(calloc(${struct.size}))")
                    .build()
            ).addFunction(
                FunSpec.builder(struct.name)
                    .receiver(MemoryStack::class)
                    .addFieldAsParameters()
                    .returns(struct.type)
                    .addStatement("return ${struct.name}(malloc(${struct.size}), ${struct.fields.joinToString { it.name }})")
                    .build()
            ).addFunction(
                FunSpec.builder("sizeof")
                    .addParameter("dummy", ClassName(struct.type.packageName, struct.name, "Companion"))
                    .returns(Long::class)
                    .addStatement("return ${struct.size}L")
                    .build()
            ).addFunction(
                FunSpec.builder("sizeof")
                    .returns(Long::class)
                    .addParameter("f", KMutableProperty1::class.asClassName().parameterizedBy(struct.type, STAR))
                    .beginControlFlow("return when (f)")
                    .apply {
                        struct.fields.forEach {
                            addStatement("${struct.name}::${it.name} -> ${it.size}L")
                        }
                    }
                    .addStatement("else -> throw IllegalArgumentException(\"Unknown field \$f\")")
                    .endControlFlow()
                    .build()
            ).addFunction(
                FunSpec.builder("offsetof")
                    .returns(Long::class)
                    .addParameter("f", KMutableProperty1::class.asClassName().parameterizedBy(struct.type, STAR))
                    .beginControlFlow("return when (f)")
                    .apply {
                        struct.fields.forEach {
                            addStatement("${struct.name}::${it.name} -> ${it.offset}L")
                        }
                    }
                    .addStatement("else -> throw IllegalArgumentException(\"Unknown field \$f\")")
                    .endControlFlow()
                    .build()
            )

        FileSpec.builder(struct.type)
            .addAnnotation(
                AnnotationSpec.get(
                    Suppress(
                        "RedundantVisibilityModifier",
                        "unused",
                        "UNUSED_PARAMETER",
                        "MemberVisibilityCanBePrivate",
                        "ReplaceArrayOfWithLiteral",
                        "SpellCheckingInspection",
                        "RemoveRedundantQualifierName"
                    )
                )
            )
            .addKotlinDefaultImports(includeJvm = true)
            .addType(
                TypeSpec.valueClassBuilder(struct.name)
                    .addAnnotation(AnnotationSpec.get(struct.structAnnotation, true))
                    .addAnnotation(JvmInline::class)
                    .addConstructor()
                    .addBasics()
                    .addOperatorFunctions()
                    .addFieldProperties()
                    .addCompanion()
                    .build()
            )
            .addHelpers()
            .indent("    ")
            .build()
            .writeTo(environment.codeGenerator, Dependencies(false))
    }

    private data class StructInfo(
        val name: String,
        val pkg: String,
        val structAnnotation: Struct,
        val fields: List<FieldInfo>,
    ) {
        val size = structAnnotation.size
        val type = ClassName(pkg, name)
    }

    private data class FieldInfo(
        val name: String,
        val type: ClassName,
        val isPrimitive: Boolean,
        val offset: Long,
        val size: Long,
        val paddingAnnotation: Padding?
    ) {
        val fieldAnnotation = Field(offset, size)
    }
}