pluginManagement {
    repositories {
        gradlePluginPortal()
    }

    val kotlinVersion: String by settings

    plugins {
        id("org.jetbrains.kotlin.jvm").version(kotlinVersion)
    }
}

include("core")
include("struct-api", "struct-processor")