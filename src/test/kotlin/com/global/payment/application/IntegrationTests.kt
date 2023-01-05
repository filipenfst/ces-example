package com.global.payment.application

import com.global.payment.application.commons.conteiners.PostgresqlInitializer
import com.global.payment.application.commons.conteiners.merge
import com.global.payment.application.commons.conteiners.resetDB
import com.global.payment.application.commons.wiremock.WireMockInitializer
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.slf4j.bridge.SLF4JBridgeHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.testcontainers.lifecycle.Startables
import java.util.TimeZone

private val initializers = setOf(
//    WireMockInitializer,
    PostgresqlInitializer
).merge()

@ContextConfiguration(
    initializers = [IntegrationTests.TestAppInitializer::class]
)
@AutoConfigureWebTestClient
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = [Application::class]
)
@TestPropertySource(properties = ["spring.config.location=classpath:application.yml"])
internal interface IntegrationTests {
    companion object {
        @BeforeAll
        fun setupTime() {
            SLF4JBridgeHandler.removeHandlersForRootLogger()
            SLF4JBridgeHandler.install()
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
        }
    }
    @BeforeEach
    fun startWiremock(){
        WireMockInitializer.start()
    }
    @AfterEach
    fun reset(
        @Autowired circuitBreakerRegistry: CircuitBreakerRegistry,
        @Autowired databaseClient: DatabaseClient,
    ): Unit = runBlocking {
        WireMockInitializer.resetAll()
        databaseClient.resetDB()
        circuitBreakerRegistry.allCircuitBreakers.forEach {
            it.reset()
        }
    }
    class TestAppInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
        override fun initialize(applicationContext: ConfigurableApplicationContext) {
            Startables.deepStart(initializers).get()
            TestPropertyValues.of(initializers.getProperties()).applyTo(applicationContext)
        }
    }
}
