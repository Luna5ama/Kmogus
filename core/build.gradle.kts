plugins {
    id("com.google.devtools.ksp")
}

dependencies {
    ksp(project(":core:codegen"))
}

publishing {
    publications {
        create<MavenPublication>(project.name) {
            artifactId = base.archivesName.get()
            from(components["java"])
        }
    }
}