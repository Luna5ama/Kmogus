@file:Suppress("UnstableApiUsage")

allprojects {
    group = "dev.luna5ama"
    version = "1.1-SNAPSHOT"

    repositories {
        mavenCentral()
        maven("https://maven.luna5ama.dev/")
    }
}

plugins {
    kotlin("jvm")
    `maven-publish`
    id("dev.fastmc.maven-repo").version("1.0.0").apply(false)
    id("dev.luna5ama.ktgen").version("1.0.1").apply(false)
}

subprojects {
    apply {
        plugin("java")
        plugin("kotlin")
        plugin("maven-publish")
        plugin("dev.fastmc.maven-repo")
    }

    base {
        archivesName.set("${rootProject.name.lowercase()}-${project.name}")
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(8))
            vendor.set(JvmVendorSpec.ADOPTIUM)
        }
        withSourcesJar()
    }

    dependencies {
        testImplementation(kotlin("test"))
    }

    kotlin {
        compilerOptions {
            freeCompilerArgs = listOf(
                "-Xjvm-default=all",
                "-Xbackend-threads=0"
            )
        }
    }

    tasks {
        test {
            project.javaToolchains {
                this@test.javaLauncher.set(launcherFor {
                    languageVersion.set(JavaLanguageVersion.of(8))
                    vendor.set(JvmVendorSpec.ADOPTIUM)
                })
            }
            useJUnitPlatform()
        }
    }
}

tasks.jar.get().isEnabled = false