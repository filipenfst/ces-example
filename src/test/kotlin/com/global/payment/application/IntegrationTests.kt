package com.global.payment.application

import com.global.payment.application.commons.conteiners.PostgresqlInitializer
import com.global.payment.application.commons.conteiners.merge
import com.global.payment.application.commons.conteiners.resetDB
import com.global.payment.application.commons.wiremock.WireMockInitializer
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import io.micronaut.data.r2dbc.operations.R2dbcOperations
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.micronaut.test.support.TestPropertyProvider
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.slf4j.bridge.SLF4JBridgeHandler
import java.util.TimeZone

private val initializers = setOf(
    WireMockInitializer,
    PostgresqlInitializer
).merge()

@MicronautTest(application = Application::class, transactional = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal interface IntegrationTests : TestPropertyProvider {
    @BeforeAll
    fun setupTime() {
        SLF4JBridgeHandler.removeHandlersForRootLogger()
        SLF4JBridgeHandler.install()
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    }

    @AfterEach
    fun reset(
        circuitBreakerRegistry: CircuitBreakerRegistry,
        r2dbcOperations: R2dbcOperations,
    ): Unit = runBlocking {
        WireMockInitializer.resetAll()
        r2dbcOperations.resetDB()
        circuitBreakerRegistry.allCircuitBreakers.forEach {
            it.reset()
        }
    }


    override fun getProperties(): Map<String, String> {
        initializers.start()
        return initializers.getProperties()
    }
}
