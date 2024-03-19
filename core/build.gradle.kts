plugins {
    id("dev.luna5ama.ktgen")
}

dependencies {
    ktgen(project("codegen"))
}

publishing {
    publications {
        create<MavenPublication>(project.name) {
            artifactId = base.archivesName.get()
            from(components["java"])
        }
    }
}