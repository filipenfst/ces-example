package com.global.payment.application.commons.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.common.ConsoleNotifier
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.global.payment.application.commons.conteiners.ContextInitializer
import com.global.payment.commons.logger.logInfo
import org.testcontainers.containers.GenericContainer

object WireMockInitializer : ContextInitializer {
    private var startWireMock = false

    val wireMock = WireMockServer(
        WireMockConfiguration
            .wireMockConfig()
            .port(8576)
//            .dynamicHttpsPort()
            .notifier(ConsoleNotifier(true))
    )

    fun resetAll() {
        wireMock.resetAll()
    }

    override fun getProperties(): Map<String, String> {
        return mapOf(
            "client.user.api.base-url" to "http://localhost:%s".format(wireMock.port()),
        ).also {
            it.values.forEach { v -> logInfo("Wiremock----------$v") }
        }
    }

    override fun start() {
        wireMock.start()
        logInfo(msg = "Wiremock started")
    }

    override fun stop() {
        wireMock.stop()
    }
}
