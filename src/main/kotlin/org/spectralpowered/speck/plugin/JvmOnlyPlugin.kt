package org.spectralpowered.speck.plugin

import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class JvmOnlyPlugin : AbstractKotlinProjectPlugin("jvm") {
    override val relevantPublications: Set<String> = setOf("kotlinOSSRH", "javaOSSRH")

    override fun Project.applyThisPlugin() {
        apply(plugin = kotlinPlugin())
        log("apply ${kotlinPlugin()} plugin")
        apply(plugin = "java-library")
        log("apply java-library plugin")
        configureKotlinVersionFromCatalogIfPossible()
        configureJvmVersionFromCatalogIfPossible()
        tasks.withType(KotlinCompile::class.java) { task ->
            task.kotlinOptions {
                configureKotlinOptions(targetCompilationId(task))
                configureJvmKotlinOptions(targetCompilationId(task))
            }
        }
        tasks.getByName("javadoc") {
            it.enabled = !getBooleanProperty("disableJavadocTask")
        }
        dependencies {
            addMainDependencies(project, target = "jdk8", skipBom = !getBooleanProperty("useKotlinBom"))
            addTestDependencies(project, target = "junit", skipAnnotations = true)
        }
        configure(JavaPluginExtension::class) {
            withSourcesJar()
            log("configure JVM library to include sources JAR")
        }
        addPlatformSpecificTaskAliases()
    }

    override fun PropertiesHelperExtension.declareProperties() {
        addProperty(disableJavadocTask)
        addProperty(allWarningsAsErrors)
        addProperty(ktCompilerArgs)
        addProperty(ktCompilerArgsJvm)
        addProperty(mochaTimeout)
        addProperty(versionsFromCatalog)
        addProperty(useKotlinBom)
    }
}