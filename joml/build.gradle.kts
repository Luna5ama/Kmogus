plugins {
    id("dev.luna5ama.ktgen")
}

dependencies {
    api(project(":core"))
    implementation("org.joml:joml:1.10.5")
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