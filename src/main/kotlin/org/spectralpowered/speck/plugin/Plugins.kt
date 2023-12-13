@file:Suppress("MaxLineLength", "ktlint")

package org.spectralpowered.speck.plugin

object Plugins {
    val bugFinder = PluginDescriptor("org.spectralpowered.speck.bug-finder", BugFinderPlugin::class)

    val documentation = PluginDescriptor("org.spectralpowered.speck.documentation", DocumentationPlugin::class)

    val linter = PluginDescriptor("org.spectralpowered.speck.linter", LinterPlugin::class)

    val mavenPublish = PluginDescriptor("org.spectralpowered.speck.maven-publish", PublishOnMavenPlugin::class)

    val npmPublish = PluginDescriptor("org.spectralpowered.speck.npm-publish", PublishOnNpmPlugin::class)

    val versions = PluginDescriptor("org.spectralpowered.speck.versions", VersionsPlugin::class)

    val jsOnly = PluginDescriptor("org.spectralpowered.speck.js-only", JsOnlyPlugin::class)

    val jvmOnly = PluginDescriptor("org.spectralpowered.speck.jvm-only", JvmOnlyPlugin::class)

    val multiplatform = PluginDescriptor("org.spectralpowered.speck.multiplatform", MultiplatformPlugin::class)

    val multiProjectHelper = PluginDescriptor("org.spectralpowered.speck.multi-project-helper", MultiProjectHelperPlugin::class)
}
