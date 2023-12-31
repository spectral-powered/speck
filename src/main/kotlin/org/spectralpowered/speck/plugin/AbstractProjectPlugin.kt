package org.spectralpowered.speck.plugin

import dev.petuska.npm.publish.extension.NpmPublishExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import java.io.File
import java.util.Locale
import kotlin.reflect.KClass

@Suppress("TooManyFunctions")
abstract class AbstractProjectPlugin : Plugin<Project> {

    companion object {
        @JvmStatic
        protected val SUPPORTED_KOTLIN_TARGETS = setOf("jvm", "js", "multiplatform")
    }

    protected abstract fun Project.applyThisPlugin()

    override fun apply(target: Project) {
        val propertiesHelper = target.extensions.run {
            findByType(PropertiesHelperExtension::class.java)
                ?: create("propertiesHelper", PropertiesHelperExtension::class.java).also {
                    target.addPropertiesHelperTasks(it)
                    target.log("add propertiesHelper extension")
                }
        }
        propertiesHelper.declareProperties()
        target.applyThisPlugin()
    }

    private fun Project.addPropertiesHelperTasks(ext: PropertiesHelperExtension) {
        val explainProperties = tasks.maybeCreate("explainProperties").run {
            group = "properties"
            doLast {
                println(ext.generateExplanatoryText())
            }
        }
        val generateGradlePropertiesFile = tasks.maybeCreate("generateGradlePropertiesFile").run {
            group = "properties"
            doLast {
                if (!ext.overwriteGradlePropertiesFile && gradlePropertiesFile.exists()) {
                    val tmp = File.createTempFile("gradle", "properties")
                    if (!tmp.renameTo(tmp)) {
                        error("Cannot move $gradlePropertiesPath to temp directory")
                    }
                    gradlePropertiesFile.bufferedWriter().use { writer ->
                        writer.append(ext.generateGradlePropertiesText())
                        tmp.bufferedReader().use { reader ->
                            reader.lines().forEach(writer::append)
                        }
                    }
                } else {
                    gradlePropertiesFile.writeText(ext.generateGradlePropertiesText())
                }
            }
        }
        log("add tasks ${explainProperties.path}, ${generateGradlePropertiesFile.path}")
    }

    protected open fun PropertiesHelperExtension.declareProperties() {
        // does nothing by default
    }

    protected fun <T : Plugin<Project>> Project.apply(klass: KClass<T>): T =
        plugins.apply(klass.java)

    protected fun <T : Any> Project.configure(klass: KClass<T>, action: T.() -> Unit) {
        extensions.getByType(klass.java).run(action)
    }

    private val Project.propertiesHelper: PropertiesHelperExtension
        get() = extensions.getByType(PropertiesHelperExtension::class.java)

    protected fun Project.getPropertyDescriptor(name: String): PropertyDescriptor =
        propertiesHelper.properties[name]
            ?: error("Unregistered property in project ${project.name}: $name")

    protected fun Project.getProperty(name: String): String {
        if (name !in properties) {
            logMissingProperty(name)
        }
        return property(name).toString()
    }

    private fun Project.logMissingProperty(name: String) {
        getPropertyDescriptor(name).logHelpIfNecessary(project)
    }

    protected fun Project.getOptionalProperty(name: String): String? =
        findProperty(name)?.toString()
            ?: getPropertyDescriptor(name).also { it.logHelpIfNecessary(project) }.defaultValue?.toString()

    protected fun Project.getBooleanProperty(name: String): Boolean =
        getOptionalProperty(name)?.toBooleanStrictOrNull()
            ?: error("Property $name has no default value, and this is a bug. Please report it.")

    protected fun Task.sibling(name: String) =
        path.split(":").let {
            it.subList(0, it.lastIndex) + name
        }.joinToString(":")

    protected val Project.isRootProject
        get() = this == rootProject

    protected fun <T : Any> ExtensionContainer.create(
        name: String,
        klass: KClass<T>,
        vararg constructorArguments: Any,
    ): T = create(name, klass.java, *constructorArguments)

    protected fun <T : Any> ExtensionContainer.getByType(klass: KClass<T>): T = getByType(klass.java)

    context(Project)
    protected fun NpmPublishExtension.syncNpmVersionWithProject() {
        version.set(
            provider {
                project.npmCompliantVersion.also {
                    log("set NPM publication version to $it")
                }
            },
        )
    }

    private fun Project.makeAssembleTaskDependOnJarTask(task: Jar) {
        tasks.matching { it.name == "assemble" }.configureEach { assemble ->
            assemble.dependsOn(task)
            log("make ${assemble.path} task dependant on ${task.path}")
        }
    }

    context(Project)
    protected fun MavenPublication.addJarTask(task: Jar) {
        artifact(task)
        log("add task ${task.path} to publication $name, as javadoc artifact")
    }

    protected fun Project.createJarTask(name: String, classifier: String, group: String, action: Jar.() -> Unit): Jar =
        tasks.maybeCreate(name, Jar::class.java).also {
            it.group = group
            it.archiveClassifier.set(classifier)
            makeAssembleTaskDependOnJarTask(it)
            it.duplicatesStrategy = DuplicatesStrategy.WARN
            it.action()
        }

    @Suppress("MagicNumber")
    protected fun String?.asField() = when {
        this == null -> "null"
        else -> "'${replace("'", "\\'")}'"
    }

    @Suppress("MagicNumber")
    protected fun String?.asPassword() = when {
        this == null -> "null"
        else -> (1..kotlin.math.min(length, 8)).map { '*' }.joinToString("")
    }

    protected fun String.capital(): String =
        replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

    protected fun Project.forAllKotlinPlugins(action: (Plugin<*>) -> Unit) {
        forEachKotlinPlugin(SUPPORTED_KOTLIN_TARGETS, action)
    }

    protected fun Project.forEachKotlinPlugin(names: Iterable<String>, action: (Plugin<*>) -> Unit) {
        forEachPlugin(names.map { kotlinPlugin(it) }, action)
    }

    protected fun Project.forEachKotlinPlugin(name: String, vararg names: String, action: (Plugin<*>) -> Unit) {
        forEachKotlinPlugin(listOf(name, *names), action)
    }
}