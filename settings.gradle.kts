pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.fastmc.dev/")
    }

    val kotlinVersion: String by settings

    plugins {
        id("org.jetbrains.kotlin.jvm").version(kotlinVersion)
    }
}

include("core", "core:codegen")
include("joml", "joml:codegen")
include("struct-api", "struct-plugin")