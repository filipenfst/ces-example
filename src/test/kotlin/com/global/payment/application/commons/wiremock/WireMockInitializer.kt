package com.global.payment.application.commons.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.common.ConsoleNotifier
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.global.payment.application.commons.conteiners.ContextInitializer

object WireMockInitializer : ContextInitializer {
    private var startWireMock = false

    val wireMock = WireMockServer(
        WireMockConfiguration
            .wireMockConfig()
            .dynamicPort()
            .dynamicHttpsPort()
            .notifier(ConsoleNotifier(true))
    )

    fun resetAll() {
        wireMock.resetAll()
    }

    override fun getProperties(): Map<String, String> {
        return mapOf(
            "client.user-api.base-url" to "http://localhost:%s".format(wireMock.port()),
        )
    }

    override fun start() {
        if (!startWireMock) {
            wireMock.start()
            startWireMock = true
        }
    }

    override fun stop() {
        wireMock.stop()
    }
}
