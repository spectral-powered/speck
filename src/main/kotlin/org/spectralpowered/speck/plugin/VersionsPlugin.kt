package org.spectralpowered.speck.plugin

import org.gradle.api.Project

class VersionsPlugin : AbstractProjectPlugin() {
    override fun Project.applyThisPlugin() {
        val versioning = "versioning"
        group = rootProject.group
        log("copy group from rootProject: $group")
        version = rootProject.version
        log("copy version from rootProject: $version")
        tasks.register("printVersion") {
            it.group = versioning
            it.doLast { println(project.version) }
        }
        tasks.register("printNpmVersion") {
            it.group = versioning
            it.doLast { println(project.npmCompliantVersion) }
        }
        log("apply ${Plugins.versions.name} plugin")
    }
}