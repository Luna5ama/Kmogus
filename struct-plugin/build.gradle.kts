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
    implementation(project(":struct-api"))
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