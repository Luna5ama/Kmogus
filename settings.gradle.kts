pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.luna5ama.dev/")
    }

    val kotlinVersion: String by settings
    val ktgenVersion: String by settings

    plugins {
        id("org.jetbrains.kotlin.jvm").version(kotlinVersion)
        id("dev.luna5ama.ktgen").version(ktgenVersion)
    }
}

include("core", "core:codegen")
include("joml", "joml:codegen")
include("struct-api", "struct-codegen", "struct-codegen-runtime")