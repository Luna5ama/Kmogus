plugins {
    id("com.google.devtools.ksp")
}

dependencies {
    api(project(":core"))
    implementation("org.joml:joml:1.10.5")
    ksp(project(":joml:codegen"))
}

publishing {
    publications {
        create<MavenPublication>(project.name) {
            artifactId = base.archivesName.get()
            from(components["java"])
        }
    }
}