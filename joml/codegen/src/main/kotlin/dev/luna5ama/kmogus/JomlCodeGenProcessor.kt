package dev.luna5ama.kmogus

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.writeTo
import org.joml.*

class JomlCodeGenProcessor(private val environment: SymbolProcessorEnvironment) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        genJomlApdaptors(resolver)
        return emptyList()
    }

    private fun genJomlApdaptors(resolver: Resolver) {
        if (resolver.getAllFiles().any { it.fileName == "JomlAdapters.kt" }) return

        FileSpec.builder("dev.luna5ama.kmogus", "JomlAdapters")
            .addAnnotation(AnnotationSpec.builder(JvmName::class).addMember(""""JomlAdapters"""").build())
            .addReadOnly()
            .addMutable()
            .build()
            .writeTo(environment.codeGenerator, Dependencies(false))
    }

    private val primSize = mapOf(
        Byte::class.java to Byte.SIZE_BYTES,
        Short::class.java to Short.SIZE_BYTES,
        Int::class.java to Int.SIZE_BYTES,
        Long::class.java to Long.SIZE_BYTES,
        Float::class.java to Float.SIZE_BYTES,
        Double::class.java to Double.SIZE_BYTES
    )

    private val setterName = mapOf(
        Byte::class.java to "setByte",
        Short::class.java to "setShort",
        Int::class.java to "setInt",
        Long::class.java to "setLong",
        Float::class.java to "setFloat",
        Double::class.java to "setDouble"
    )

    private val getterName = mapOf(
        Byte::class.java to "getByte",
        Short::class.java to "getShort",
        Int::class.java to "getInt",
        Long::class.java to "getLong",
        Float::class.java to "getFloat",
        Double::class.java to "getDouble"
    )

    private val v2 = listOf(
        "x", "y"
    )

    private val v3 = listOf(
        "x", "y", "z"
    )

    private val v4 = listOf(
        "x", "y", "z", "w"
    )

    private val readOnlyVector = listOf(
        calcClassInfo(Vector2ic::class.java, v2),
        calcClassInfo(Vector3ic::class.java, v3),
        calcClassInfo(Vector4ic::class.java, v4),

        calcClassInfo(Vector2fc::class.java, v2),
        calcClassInfo(Vector3fc::class.java, v3),
        calcClassInfo(Vector4fc::class.java, v4),

        calcClassInfo(Vector2dc::class.java, v2),
        calcClassInfo(Vector3dc::class.java, v3),
        calcClassInfo(Vector4dc::class.java, v4)
    )

    private val mutableVector = listOf(
        calcClassInfo(Vector2i::class.java, v2),
        calcClassInfo(Vector3i::class.java, v3),
        calcClassInfo(Vector4i::class.java, v4),

        calcClassInfo(Vector2f::class.java, v2),
        calcClassInfo(Vector3f::class.java, v3),
        calcClassInfo(Vector4f::class.java, v4),

        calcClassInfo(Vector2d::class.java, v2),
        calcClassInfo(Vector3d::class.java, v3),
        calcClassInfo(Vector4d::class.java, v4)
    )

    private val m2 = listOf(
        "m00", "m01",
        "m10", "m11"
    )
    private val m3 = listOf(
        "m00", "m01", "m02",
        "m10", "m11", "m12",
        "m20", "m21", "m22"
    )
    private val m4 = listOf(
        "m00", "m01", "m02", "m03",
        "m10", "m11", "m12", "m13",
        "m20", "m21", "m22", "m23",
        "m30", "m31", "m32", "m33"
    )
    private val m3x2 = listOf(
        "m00", "m01",
        "m10", "m11",
        "m20", "m21"
    )
    private val m4x3 = listOf(
        "m00", "m01", "m02",
        "m10", "m11", "m12",
        "m20", "m21", "m22",
        "m30", "m31", "m32"
    )

    private val readOnlyMatrix = listOf(
        calcClassInfo(Matrix2fc::class.java, m2),
        calcClassInfo(Matrix3fc::class.java, m3),
        calcClassInfo(Matrix4fc::class.java, m4),
        calcClassInfo(Matrix3x2fc::class.java, m3x2),
        calcClassInfo(Matrix4x3fc::class.java, m4x3),

        calcClassInfo(Matrix2dc::class.java, m2),
        calcClassInfo(Matrix3dc::class.java, m3),
        calcClassInfo(Matrix4dc::class.java, m4),
        calcClassInfo(Matrix3x2dc::class.java, m3x2),
        calcClassInfo(Matrix4x3dc::class.java, m4x3)
    )

    private val mutableMatrix = listOf(
        calcClassInfo(Matrix2f::class.java, m2),
        calcClassInfo(Matrix3f::class.java, m3),
        calcClassInfo(Matrix4f::class.java, m4),
        calcClassInfo(Matrix3x2f::class.java, m3x2),
        calcClassInfo(Matrix4x3f::class.java, m4x3),

        calcClassInfo(Matrix2d::class.java, m2),
        calcClassInfo(Matrix3d::class.java, m3),
        calcClassInfo(Matrix4d::class.java, m4),
        calcClassInfo(Matrix3x2d::class.java, m3x2),
        calcClassInfo(Matrix4x3d::class.java, m4x3)
    )

    private fun calcClassInfo(clazz: Class<*>, fieldNames: List<String>): ClassInfo {
        var offset = 0
        val fieldInfos = fieldNames.asSequence()
            .map { clazz.getMethod(it) }
            .map {
                val size = primSize[it.returnType]
                val info = FieldInfo(it.name, it.returnType, offset, size!!)
                offset += size
                info
            }
            .toList()

        return ClassInfo(clazz, fieldNames, fieldInfos)
    }

    private fun FunSpec.Builder.slowToPtr(classInfo: ClassInfo, src: String): FunSpec.Builder {
        try {
            classInfo.fieldNames.forEach {
                classInfo.clazz.getField(it)
            }

            classInfo.fieldInfos.forEach {
                val setterName = setterName[it.type]!!

                if (it.offset == 0) {
                    addStatement("ptr.$setterName($src.${it.name})")
                } else {
                    addStatement("ptr.$setterName(${longLiteral(it.offset)}, $src.${it.name})")
                }
            }
        } catch (e: NoSuchFieldException) {
            classInfo.fieldInfos.forEach {
                val setterName = setterName[it.type]!!

                if (it.offset == 0) {
                    addStatement("ptr.$setterName($src.${it.name}())")
                } else {
                    addStatement("ptr.$setterName(${longLiteral(it.offset)}, $src.${it.name}())")
                }
            }
        }

        return this
    }

    private fun FunSpec.Builder.slowFromPtr(classInfo: ClassInfo, src: String): FunSpec.Builder {
        try {
            classInfo.fieldNames.forEach {
                classInfo.clazz.getField(it)
            }

            fromPtrFields(classInfo, src)
        } catch (e: NoSuchFieldException) {
            fromPtrSet(src, classInfo)
        }

        return this
    }

    private fun FunSpec.Builder.fromPtrFields(
        classInfo: ClassInfo,
        src: String
    ) {
        classInfo.fieldInfos.forEach {
            val getterName = getterName[it.type]!!

            if (it.offset == 0) {
                addStatement("$src.${it.name} = ptr.$getterName()")
            } else {
                addStatement("$src.${it.name} = ptr.$getterName(${longLiteral(it.offset)})")
            }

            updatePropCall(classInfo, src)
        }
    }

    private fun FunSpec.Builder.fromPtrSet(
        src: String,
        classInfo: ClassInfo
    ) {
        addCode(
            "$src.set(${
                classInfo.fieldInfos.joinToString(",\n", prefix = "\n", postfix = "\n") {
                    val getterName = getterName[it.type]!!

                    if (it.offset == 0) {
                        "ptr.${getterName}()"
                    } else {
                        "ptr.${getterName}(${longLiteral(it.offset)})"
                    }
                }
            })"
        )
    }

    private fun FunSpec.Builder.updatePropCall(
        classInfo: ClassInfo,
        src: String
    ) {
        runCatching {
            classInfo.clazz.getMethod("determineProperties")
        }.onSuccess {
            addStatement("$src.determineProperties()")
        }
    }

    private fun codeTrySelectFast(
        lowerCaseName: String,
        classInfo: ClassInfo
    ): String {
        return "selectAdapter(${lowerCaseName}Fast, ${lowerCaseName}Slow, ${classInfo.fieldNames.asLiteral()})"
    }

    private fun longLiteral(value: Int) = "${value}L"

    private fun List<String>.asLiteral() = joinToString(", ") { "\"$it\"" }

    private fun FileSpec.Builder.addReadOnly(): FileSpec.Builder {
        val list = readOnlyVector + readOnlyMatrix
        list.forEach { classInfo ->
            addFunction(
                FunSpec.builder("copyTo")
                    .addParameter("ptr", Ptr::class)
                    .receiver(classInfo.clazz)
                    .slowToPtr(classInfo, "this")
                    .build()
            )

            addFunction(
                FunSpec.builder("copyToMutableArr")
                    .addParameter("arr", MutableArr::class)
                    .receiver(classInfo.clazz)
                    .addStatement("this.copyTo(arr.ptr)")
                    .addStatement("arr.pos += ${classInfo.size}")
                    .build()
            )
        }

        return this
    }

    private fun FileSpec.Builder.addMutable(): FileSpec.Builder {
        val list = mutableVector + mutableMatrix
        list.forEach { classInfo ->
            val adapterType = ClassName.bestGuess("dev.luna5ama.kmogus.IAdapter").parameterizedBy(classInfo.className)
            val lowerCaseName = classInfo.simpleName.lowercase()

            addProperty(
                PropertySpec.builder("${lowerCaseName}Slow", adapterType, KModifier.PRIVATE)
                    .initializer(
                        TypeSpec.anonymousClassBuilder()
                            .addSuperinterface(adapterType)
                            .addFunction(
                                FunSpec.builder("copyTo")
                                    .addModifiers(KModifier.OVERRIDE)
                                    .addParameter("o", classInfo.className)
                                    .addParameter("ptr", Ptr::class)
                                    .slowToPtr(classInfo, "o")
                                    .build()
                            )
                            .addFunction(
                                FunSpec.builder("copyFrom")
                                    .addModifiers(KModifier.OVERRIDE)
                                    .addParameter("o", classInfo.className)
                                    .addParameter("ptr", Ptr::class)
                                    .slowFromPtr(classInfo, "o")
                                    .build()
                            )
                            .build().toString()
                    )
                    .build()
            )

            addProperty(
                PropertySpec.builder("${lowerCaseName}Fast", adapterType, KModifier.PRIVATE)
                    .initializer(
                        TypeSpec.anonymousClassBuilder()
                            .addSuperinterface(adapterType)
                            .apply {
                                classInfo.fastCopyInfo.forEach {
                                    addProperty(
                                        PropertySpec.builder(it.offsetName, Long::class)
                                            .initializer("""getOffset(${classInfo.simpleName}::class, "${it.fieldName}")""")
                                            .build()
                                    )
                                }
                            }
                            .addFunction(
                                FunSpec.builder("copyTo")
                                    .addModifiers(KModifier.OVERRIDE)
                                    .addParameter("o", classInfo.className)
                                    .addParameter("ptr", Ptr::class)
                                    .apply {
                                        classInfo.fastCopyInfo.forEach {
                                            val setterName = setterName[it.type]!!
                                            val getterName = getterName[it.type]!!
                                            addStatement("ptr.$setterName(${it.offset}, UNSAFE.$getterName(o, ${it.offsetName}))")
                                        }
                                    }
                                    .build()
                            )
                            .addFunction(
                                FunSpec.builder("copyFrom")
                                    .addModifiers(KModifier.OVERRIDE)
                                    .addParameter("o", classInfo.className)
                                    .addParameter("ptr", Ptr::class)
                                    .apply {
                                        classInfo.fastCopyInfo.forEach {
                                            val setterName = setterName[it.type]!!.replace("set", "put")
                                            val getterName = getterName[it.type]!!
                                            addStatement("UNSAFE.$setterName(o, ${it.offsetName}, ptr.$getterName(${if (it.offset == 0) "" else it.offset}))")
                                        }

                                        updatePropCall(classInfo, "o")
                                    }
                                    .build()
                            )
                            .build().toString()
                    )
                    .build()
            )

            addProperty(
                PropertySpec.builder(lowerCaseName, adapterType, KModifier.PRIVATE)
                    .mutable(true)
                    .initializer(codeTrySelectFast(lowerCaseName, classInfo))
                    .build()
            )

            addFunction(
                FunSpec.builder("copyTo")
                    .addParameter("ptr", Ptr::class)
                    .receiver(classInfo.clazz)
                    .addStatement("$lowerCaseName.copyTo(this, ptr)")
                    .build()
            )

            addFunction(
                FunSpec.builder("copyToMutableArr")
                    .addParameter("arr", MutableArr::class)
                    .receiver(classInfo.clazz)
                    .addStatement("this.copyTo(arr.ptr)")
                    .addStatement("arr.pos += ${classInfo.size}")
                    .build()
            )

            addFunction(
                FunSpec.builder("copyFrom")
                    .addParameter("ptr", Ptr::class)
                    .receiver(classInfo.clazz)
                    .addStatement("$lowerCaseName.copyFrom(this, ptr)")
                    .build()
            )
        }

        addFunction(
            FunSpec.builder("useOptimal")
                .addModifiers(KModifier.INTERNAL)
                .apply {
                    list.forEach { classInfo ->
                        val lowerCaseName = classInfo.simpleName.lowercase()
                        addStatement("$lowerCaseName = ${codeTrySelectFast(lowerCaseName, classInfo)}")
                    }
                }
                .build()
        )

        addFunction(
            FunSpec.builder("useFast")
                .addModifiers(KModifier.INTERNAL)
                .apply {
                    list.forEach { classInfo ->
                        val lowerCaseName = classInfo.simpleName.lowercase()
                        addStatement("$lowerCaseName = ${lowerCaseName}Fast")
                    }
                }
                .build()
        )

        addFunction(
            FunSpec.builder("useSlow")
                .addModifiers(KModifier.INTERNAL)
                .apply {
                    list.forEach { classInfo ->
                        val lowerCaseName = classInfo.simpleName.lowercase()
                        addStatement("$lowerCaseName = ${lowerCaseName}Slow")
                    }
                }
                .build()
        )

        return this
    }

    private class ClassInfo(
        val clazz: Class<*>,
        val fieldNames: List<String>,
        val fieldInfos: List<FieldInfo>,
    ) {
        val simpleName = clazz.simpleName

        @OptIn(DelicateKotlinPoetApi::class)
        val className = clazz.asClassName()
        val size: Int = fieldInfos.sumOf { it.size }

        val fastCopyInfo: List<FastCopyInfo>

        init {
            var offset = 0
            var nameIndex = 1
            var fieldIndex = 0
            val fastCopyInfo = mutableListOf<FastCopyInfo>()

            while (fieldIndex in fieldInfos.indices) {
                val fieldInfo = fieldInfos[fieldIndex]
                if (fieldInfo.offset < offset) {
                    fieldIndex++
                    continue
                }

                if (size - offset < 8) break

                fastCopyInfo.add(FastCopyInfo("offset${nameIndex}", fieldInfo.name, Long::class.java, offset))
                nameIndex++
                offset += 8
            }

            val remaining = size - offset
            check(remaining == 0 || remaining == 4) {
                "Invalid size for ${simpleName}: $remaining/$size"
            }

            if (remaining == 4) {
                fastCopyInfo.add(
                    FastCopyInfo(
                        "offset${nameIndex}",
                        fieldInfos[fieldIndex].name,
                        Int::class.java,
                        offset
                    )
                )
            }

            this.fastCopyInfo = fastCopyInfo
        }
    }

    private data class FieldInfo(
        val name: String,
        val type: Class<*>,
        val offset: Int,
        val size: Int
    )

    private data class FastCopyInfo(
        val offsetName: String,
        val fieldName: String,
        val type: Class<*>,
        val offset: Int
    )
}