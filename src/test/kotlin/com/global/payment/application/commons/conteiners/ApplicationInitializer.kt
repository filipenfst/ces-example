package com.global.payment.application.commons.conteiners

import org.testcontainers.lifecycle.Startable
import org.testcontainers.lifecycle.Startables

abstract class ApplicationInitializer(
    private val config: ApplicationContainerInitializerConfig<*>
) : ContextInitializer {

    override fun getProperties(): Map<String, String> {
        return config.getRecursiveContainerDependencies().flatMap { it.getProperties().asIterable() }
            .associate { it.key to it.value }
    }

    override fun start() {
        Startables.deepStart(config).get()
    }

    override fun stop() {
        config.stop()
    }
}

internal interface ContextInitializer : Startable {
    fun getProperties(): Map<String, String>
}

internal fun Collection<ContextInitializer>.merge() = object : ContextInitializer {
    override fun getProperties(): Map<String, String> {
        return this@merge.flatMap { it.getProperties().entries }.associate { it.key to it.value }
    }

    override fun start() {
        Startables.deepStart(this@merge).get()
    }

    override fun stop() {
        this@merge.forEach(ContextInitializer::close)
    }

}
