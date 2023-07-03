package dev.luna5ama.kmogus

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ksp.writeTo

class CoreCodeGenProcessor(private val environment: SymbolProcessorEnvironment) : SymbolProcessor {
    private val primitiveTypes = listOf(
        Byte::class to ByteArray::class,
        Short::class to ShortArray::class,
        Char::class to CharArray::class,
        Int::class to IntArray::class,
        Long::class to LongArray::class,
        Float::class to FloatArray::class,
        Double::class to DoubleArray::class,
    )

    override fun process(resolver: Resolver): List<KSAnnotated> {
        genMemcpy(resolver)
        return emptyList()
    }

    private fun genMemcpy(resolver: Resolver) {
        if (resolver.getAllFiles().any { it.fileName == "Memcpy.kt" }) return

        FileSpec.builder("dev.luna5ama.kmogus", "Memcpy")
            .heap2heap()
            .heap2pointer()
            .pointer2Pointer()
            .build()
            .writeTo(environment.codeGenerator, Dependencies(false))
    }

    private fun FileSpec.Builder.heap2heap(): FileSpec.Builder {
        for ((src, srcArray) in primitiveTypes) {
            for ((dst, dstArray) in primitiveTypes) {
                val srcArrayOffset = "${src.simpleName!!.uppercase()}_ARRAY_OFFSET"
                val dstArrayOffset = "${dst.simpleName!!.uppercase()}_ARRAY_OFFSET"

                addFunction(
                    FunSpec.builder("memcpy")
                        .addParameter("src", srcArray)
                        .addParameter("dst", dstArray)
                        .addParameter("length", Long::class)
                        .addStatement("UNSAFE.copyMemory(src, $srcArrayOffset, dst, $dstArrayOffset, length)")
                        .build()
                )

                addFunction(
                    FunSpec.builder("memcpy")
                        .addParameter("src", srcArray)
                        .addParameter("srcOffset", Long::class)
                        .addParameter("dst", dstArray)
                        .addParameter("dstOffset", Long::class)
                        .addParameter("length", Long::class)
                        .addStatement("UNSAFE.copyMemory(src, $srcArrayOffset + srcOffset, dst, $dstArrayOffset + dstOffset, length)")
                        .build()
                )
            }
        }

        return this
    }

    private fun FileSpec.Builder.heap2pointer(): FileSpec.Builder {
        for ((p, pArray) in primitiveTypes) {
            val pArrayOffset = "${p.simpleName!!.uppercase()}_ARRAY_OFFSET"

            addFunction(
                FunSpec.builder("memcpy")
                    .addParameter("src", ClassName("dev.luna5ama.kmogus", "Ptr"))
                    .addParameter("dst", pArray)
                    .addParameter("length", Long::class)
                    .addStatement("UNSAFE.copyMemory(null, src.address, dst, $pArrayOffset, length)")
                    .build()
            )

            addFunction(
                FunSpec.builder("memcpy")
                    .addParameter("src", ClassName("dev.luna5ama.kmogus", "Ptr"))
                    .addParameter("srcOffset", Long::class)
                    .addParameter("dst", pArray)
                    .addParameter("dstOffset", Long::class)
                    .addParameter("length", Long::class)
                    .addStatement("UNSAFE.copyMemory(null, src.address + srcOffset, dst, $pArrayOffset + dstOffset, length)")
                    .build()
            )

            addFunction(
                FunSpec.builder("memcpy")
                    .addParameter("src", pArray)
                    .addParameter("dst", ClassName("dev.luna5ama.kmogus", "Ptr"))
                    .addParameter("length", Long::class)
                    .addStatement("UNSAFE.copyMemory(src, $pArrayOffset, null, dst.address, length)")
                    .build()
            )

            addFunction(
                FunSpec.builder("memcpy")
                    .addParameter("src", pArray)
                    .addParameter("srcOffset", Long::class)
                    .addParameter("dst", ClassName("dev.luna5ama.kmogus", "Ptr"))
                    .addParameter("dstOffset", Long::class)
                    .addParameter("length", Long::class)
                    .addStatement("UNSAFE.copyMemory(src, $pArrayOffset + srcOffset, null, dst.address + dstOffset, length)")
                    .build()
            )
        }

        return this
    }

    private fun FileSpec.Builder.pointer2Pointer(): FileSpec.Builder {
        return addFunction(
            FunSpec.builder("memcpy")
                .addParameter("src", ClassName("dev.luna5ama.kmogus", "Ptr"))
                .addParameter("dst", ClassName("dev.luna5ama.kmogus", "Ptr"))
                .addParameter("length", Long::class)
                .addStatement("UNSAFE.copyMemory(null, src.address, dst, dst.address, length)")
                .build()
        ).addFunction(
            FunSpec.builder("memcpy")
                .addParameter("src", ClassName("dev.luna5ama.kmogus", "Ptr"))
                .addParameter("srcOffset", Long::class)
                .addParameter("dst", ClassName("dev.luna5ama.kmogus", "Ptr"))
                .addParameter("dstOffset", Long::class)
                .addParameter("length", Long::class)
                .addStatement("UNSAFE.copyMemory(null, src.address + srcOffset, dst, dst.address + dstOffset, length)")
                .build()
        )
    }
}