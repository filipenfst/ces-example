package com.global.payment.application.commons.conteiners

import org.testcontainers.containers.GenericContainer
import org.testcontainers.lifecycle.Startable
import java.util.Stack

interface ApplicationContainerInitializerConfig<T : GenericContainer<T>> : Startable {
    val container: T
    val applicationInitializer: ApplicationInitializer
        get() = object : ApplicationInitializer(this) {}

    fun getProperties(): Map<String, String> = emptyMap()

    fun getRecursiveContainerDependencies(): Set<ApplicationContainerInitializerConfig<*>> {
        val configs = mutableSetOf<ApplicationContainerInitializerConfig<*>>()

        val stack = Stack<ApplicationContainerInitializerConfig<*>>()
        stack.push(this)

        while (stack.isNotEmpty()) {
            val current = stack.pop()

            if (!configs.contains(current)) {
                configs.add(current)
                current.getContainerDependencies().forEach {
                    stack.push(it)
                }
            }
        }

        return configs
    }

    fun getContainerDependencies(): Set<ApplicationContainerInitializerConfig<*>> = emptySet()
    override fun getDependencies(): Set<Startable> = getContainerDependencies()

    override fun start() = container.start()
    override fun stop() = container.stop()
}
