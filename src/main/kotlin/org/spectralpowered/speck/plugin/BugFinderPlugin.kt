package org.spectralpowered.speck.plugin

import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektPlugin
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Project

class BugFinderPlugin : AbstractProjectPlugin() {
    override fun Project.applyThisPlugin() {
        val detekt = apply(DetektPlugin::class)
        log("apply ${detekt::class.java.name} as bug finder")
        configure(DetektExtension::class) {
            config.setFrom(rootProject.files(".detekt.yml"))
            log("configure bug finder from files: ${config.files.joinToString()}")
            buildUponDefaultConfig = true
        }
        val detektAll = tasks.maybeCreate("detektAll").also { it.group = "verification" }
        tasks.withType(Detekt::class.java)
            .matching { task -> task.name.let { it.endsWith("Main") || it.endsWith("Test") } }
            .all { detektAll.dependsOn(it) }
        tasks.matching { it.name == "check" }.all {
            it.dependsOn(detektAll)
            log("add task ${detektAll.path} and make ${it.path} depend on it")
        }
    }
}