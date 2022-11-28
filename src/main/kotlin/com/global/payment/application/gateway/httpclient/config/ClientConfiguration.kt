package com.global.payment.application.gateway.httpclient.config

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import io.micronaut.context.annotation.EachProperty
import io.micronaut.context.annotation.Parameter
import io.micronaut.serde.annotation.Serdeable
import jakarta.inject.Inject

@Serdeable
@EachProperty("client")
data class ClientConfiguration(
    @param:Parameter private val name: String,
    @Inject private val circuitBreakerRegistry: CircuitBreakerRegistry
) {
    val circuitBreaker = circuitBreakerRegistry.circuitBreaker(name)
}
