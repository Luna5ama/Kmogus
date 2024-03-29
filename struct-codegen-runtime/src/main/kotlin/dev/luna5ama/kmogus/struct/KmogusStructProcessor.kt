package dev.luna5ama.kmogus.struct

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import dev.luna5ama.kmogus.Arr
import dev.luna5ama.kmogus.MemoryStack
import dev.luna5ama.kmogus.MutableArr
import dev.luna5ama.kmogus.Ptr
import dev.luna5ama.ktgen.KtgenProcessor
import org.objectweb.asm.ClassReader
import org.objectweb.asm.tree.ClassNode
import sun.misc.Unsafe
import java.nio.file.Path
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.jvm.jvmErasure

class KmogusStructProcessor : KtgenProcessor {
    override fun process(inputs: List<Path>, outputDir: Path) {
        val classes = inputs.asSequence()
            .flatMap { it.toFile().walk() }
            .filter { it.extension == "class" }
            .map {
                ClassNode().apply {
                    ClassReader(it.readBytes()).accept(this, 0)
                }
            }.mapNotNull {
                try {
                    Class.forName(it.name.replace('/', '.')).kotlin
                } catch (e: ClassNotFoundException) {
                    e.printStackTrace()
                    null
                }
            }.toList()

        val validTypes = mutableMapOf<String, KClass<*>>()

        val analyzeResults = mutableMapOf<String, StructInfo>()
        val analyzeStruct = DeepRecursiveFunction<KClass<*>, StructInfo> { structClazz ->
            analyzeResults.getOrPut(structClazz.qualifiedName!!) {
                val packageName = structClazz.qualifiedName!!.substringBeforeLast('.')
                val simpleName = structClazz.simpleName!!

                val structAnnotation = structClazz.annotations.filterIsInstance<Struct>().first()
                val sizeAlignment = structAnnotation.sizeAlignment
                val fieldAlignment = structAnnotation.fieldAlignment

                var offset = 0L
                val fields = mutableListOf<FieldInfo>()

                val properties = structClazz.declaredMemberProperties.associateBy { it.name }
                for (field in structClazz.java.declaredFields) {
                    val property = properties[field.name]!!
                    val name = property.name
                    val rType = property.returnType.jvmErasure

                    property.annotations.filterIsInstance<Padding>().firstOrNull()?.let {
                        val size = it.size
                        offset += size
                    }

                    var isPrimitive = true

                    val fieldSize = when (rType) {
                        Boolean::class -> 1L
                        Byte::class -> 1L
                        Short::class -> 2L
                        Char::class -> 2L
                        Int::class -> 4L
                        Long::class -> 8L
                        Float::class -> 4L
                        Double::class -> 8L
                        else -> {
                            isPrimitive = false
                            val qualifiedName = rType.qualifiedName!!
                            validTypes[qualifiedName]?.let {
                                callRecursive(it).size
                            } ?: throw RuntimeException("Unknown field type: $qualifiedName")
                        }
                    }

                    if (fieldAlignment) {
                        offset = (offset + fieldSize - 1) / fieldSize * fieldSize
                    }

                    fields.add(
                        FieldInfo(
                            name,
                            rType.asClassName(),
                            isPrimitive,
                            offset,
                            fieldSize
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

        classes.asSequence()
            .filter { clazz ->
                clazz.annotations.asSequence().filterIsInstance<Struct>().any()
            }.forEach {
                validTypes[it.qualifiedName!!] = it
            }

        validTypes.values.map {
            analyzeStruct.invoke(it)
        }.forEach {
            genStruct(it, outputDir)
        }
    }

    @OptIn(DelicateKotlinPoetApi::class)
    private fun genStruct(struct: StructInfo, outputDir: Path) {
        fun TypeSpec.Builder.addConstructor() =
            primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter("address", Long::class)
                    .build()
            ).addFunction(
                FunSpec.constructorBuilder()
                    .addParameter("container", Arr::class)
                    .callThisConstructor("container.ptr.address")
                    .build()
            ).addFunction(
                FunSpec.constructorBuilder()
                    .addParameter("container", MutableArr::class)
                    .callThisConstructor("container.ptr.address")
                    .build()
            )

        fun TypeSpec.Builder.addBasics() =
            addProperty(
                PropertySpec.builder("address", Long::class)
                    .initializer("address")
                    .build()
            ).addProperty(
                PropertySpec.builder("ptr", Ptr::class)
                    .getter(
                        FunSpec.getterBuilder()
                            .addStatement("return Ptr(address)")
                            .build()
                    )
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
                FunSpec.builder("set")
                    .addModifiers(KModifier.OPERATOR)
                    .addParameter("index", Int::class)
                    .addParameter("value", struct.type)
                    .addStatement("UNSAFE.copyMemory(value.address, address + index.toLong() * size, size)")
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
                        .addParameter("container", Arr::class)
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
                        .addParameter("container", MutableArr::class)
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
                        .addParameter("ptr", Ptr::class)
                        .returns(struct.type)
                        .addStatement("return ${struct.name}(ptr.address)")
                        .build()
                ).addFunction(
                    FunSpec.builder("invoke")
                        .addAnnotation(JvmStatic::class)
                        .addModifiers(KModifier.OPERATOR)
                        .addParameter("ptr", Ptr::class)
                        .addFieldAsParameters()
                        .returns(struct.type)
                        .addCode(
                            CodeBlock.builder()
                                .addStatement("val v = ${struct.name}(ptr.address)")
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
                TypeSpec.classBuilder(struct.name)
                    .addModifiers(KModifier.VALUE)
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
            .writeTo(outputDir)
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
        val size: Long
    ) {
        val fieldAnnotation = Field(offset, size)
    }
}