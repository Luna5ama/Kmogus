package dev.luna5ama.kmogus.struct

import com.google.devtools.ksp.gradle.KspTaskJvm
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer

class KmogusStructPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val sourceSets = project.extensions.getByName("sourceSets") as SourceSetContainer
        val structs = sourceSets.create("structs")

        project.dependencies.add("implementation", "dev.luna5ama:kmogus-struct-api:$version")
        project.dependencies.add("ksp", "dev.luna5ama:kmogus-struct-plugin:$version")
        project.dependencies.add(structs.implementationConfigurationName, "dev.luna5ama:kmogus-struct-api:$version")

        project.afterEvaluate {
            project.tasks.named("kspStructsKotlin") {
                it.enabled = false
            }

            project.tasks.named("kspKotlin", KspTaskJvm::class.java) {
                it.source(structs.allSource.srcDirs)
            }
        }
    }

    companion object {
        val version = KmogusStructPlugin::class.java.getResource("/kmogus-struct-plugin.version")!!.readText()
    }
}