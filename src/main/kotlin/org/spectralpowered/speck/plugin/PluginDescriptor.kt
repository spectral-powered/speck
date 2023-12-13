package org.spectralpowered.speck.plugin

import kotlin.reflect.KClass

data class PluginDescriptor<T : Any>(val name: String, val klass: KClass<T>? = null)