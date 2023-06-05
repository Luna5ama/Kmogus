@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    `maven-publish`
    id("dev.fastmc.maven-repo").version("1.0.0").apply(false)
}

repositories {
    mavenCentral()
}

subprojects {
    group = "dev.luna5ama"
    version = "1.0.0-SNAPSHOT"

    apply {
        plugin("java")
        plugin("kotlin")
        plugin("maven-publish")
        plugin("dev.fastmc.maven-repo")
    }

    base {
        archivesName.set("${rootProject.name}-${project.name}")
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

    publishing {
        publications {
            create<MavenPublication>(project.name) {
                artifactId = "${rootProject.name}-${project.name}"
                from(components["java"])
            }
        }
    }
}

tasks.jar.get().isEnabled = false