plugins{
    `java-gradle-plugin`
}

gradlePlugin {
    plugins {
        create("kmogus-struct-codegen") {
            id = "dev.luna5ama.kmogus-struct-codegen"
            displayName = "kmogus-struct-codegen"
            implementationClass = "dev.luna5ama.kmogus.struct.KmogusStructCodegenPlugin"
        }
    }
}

dependencies {
    implementation(project(":struct-api"))
    implementation("dev.luna5ama:ktgen:1.0.0")
}

tasks {
    processResources {
        outputs.upToDateWhen { false }
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