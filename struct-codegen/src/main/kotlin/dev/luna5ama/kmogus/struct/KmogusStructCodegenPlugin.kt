package dev.luna5ama.kmogus.struct

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer

class KmogusStructCodegenPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.plugins.apply("dev.luna5ama.ktgen")

        val sourceSets = project.extensions.getByName("sourceSets") as SourceSetContainer
        val structs = sourceSets.create("structs")

        project.dependencies.add("ktgen", "dev.luna5ama:kmogus-struct-codegen-runtime:$version")
        project.dependencies.add("ktgen", structs.output)
        project.dependencies.add("ktgenInput", structs.output)
        project.dependencies.add(structs.implementationConfigurationName, "dev.luna5ama:kmogus-struct-api:$version")

        project.dependencies.add("api", "dev.luna5ama:kmogus-struct-api:$version")
        project.dependencies.add("api", "dev.luna5ama:kmogus-core:$version")

        project.afterEvaluate {
            project.tasks.findByName("sourceJar")?.dependsOn("ktgen")
        }
    }

    companion object {
        val version = KmogusStructCodegenPlugin::class.java.getResource("/kmogus.version.txt")!!.readText()
    }
}