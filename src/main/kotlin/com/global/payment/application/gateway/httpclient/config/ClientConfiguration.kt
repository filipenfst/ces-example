package com.global.payment.application.gateway.httpclient.config

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import io.micronaut.context.annotation.EachProperty
import io.micronaut.context.annotation.Parameter
import io.micronaut.http.client.HttpClient
import io.micronaut.serde.annotation.Serdeable
import jakarta.inject.Inject
import java.net.URL

@Serdeable
@EachProperty("client")
data class ClientConfiguration(
    @param:Parameter val name: String,
    @Inject private val circuitBreakerRegistry: CircuitBreakerRegistry
) {
    private lateinit var baseUrl: String
    val circuitBreaker = circuitBreakerRegistry.circuitBreaker(name)
    fun httpClient():HttpClient = HttpClient.create(URL(baseUrl))
}
