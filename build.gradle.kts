@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

allprojects {
    group = "dev.luna5ama"
    version = "1.0.0-SNAPSHOT"

    apply {
        plugin("java")
        plugin("kotlin")
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(8))
            vendor.set(JvmVendorSpec.ADOPTIUM)
        }
        withSourcesJar()
    }

    repositories {
        mavenCentral()
    }

    dependencies {
        testImplementation(kotlin("test"))
    }

    tasks {
        test {
            javaToolchains {
                this@test.javaLauncher.set(launcherFor {
                    languageVersion.set(JavaLanguageVersion.of(8))
                    vendor.set(JvmVendorSpec.ADOPTIUM)
                })
            }
            useJUnitPlatform()
        }

        withType<KotlinCompile> {
            kotlinOptions {
                freeCompilerArgs += listOf(
                    "-Xlambdas=indy",
                    "-Xjvm-default=all",
                    "-Xbackend-threads=0"
                )
            }
        }
    }
}