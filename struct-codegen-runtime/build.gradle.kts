dependencies {
    val ktgenVersion: String by project
    implementation(project(":struct-api"))
    implementation("dev.luna5ama:ktgen-api:$ktgenVersion")
    implementation("org.ow2.asm:asm-tree:9.7")
    kotlin("reflect")
    implementation(kotlin("reflect"))
}

publishing {
    publications {
        create<MavenPublication>(project.name) {
            artifactId = base.archivesName.get()
            from(components["java"])
        }
    }
}