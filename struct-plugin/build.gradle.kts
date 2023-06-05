plugins{
    `java-gradle-plugin`
}

gradlePlugin {
    plugins {
        create("kmogus-struct-plugin") {
            id = "dev.luna5ama.kmogus-struct-plugin"
            displayName = "kmogus-struct-plugin"
            implementationClass = "dev.luna5ama.kmogus.struct.KmogusStructPlugin"
        }
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.21")
    implementation("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:1.8.21-1.0.11")

    implementation("com.google.devtools.ksp:symbol-processing-api:1.8.20-1.0.10")
    implementation("com.squareup:kotlinpoet:1.14.2")
    implementation("com.squareup:kotlinpoet-ksp:1.14.2")
    implementation(project(":struct-api"))
}

tasks {
    processResources {
        expand("version" to project.version)
    }
}

afterEvaluate {
    publishing {
        publications {
            forEach {
                (it as MavenPublication)
                if (it.artifactId == project.name) {
                    it.artifactId = base.archivesName.get()
                } else {
                    it.pom.withXml {
                        val elements = asElement().getElementsByTagName("artifactId")
                        for (i in 0 until elements.length) {
                            val element = elements.item(i)
                            if (element.textContent == project.name) {
                                element.textContent = base.archivesName.get()
                            }
                        }
                    }
                }
            }
        }
    }
}