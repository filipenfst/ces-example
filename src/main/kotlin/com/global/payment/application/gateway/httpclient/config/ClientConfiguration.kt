package com.global.payment.application.gateway.httpclient.config

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import io.github.resilience4j.micronaut.circuitbreaker.CircuitBreakerProperties
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.EachProperty
import io.micronaut.context.annotation.Factory
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

@Factory
class CircuitBreaker(
    @Inject private val circuitBreakerProperties: CircuitBreakerProperties
){
    @Bean
    fun circuitBreakerRegistry() {
        val configs = circuitBreakerProperties.configs
        return CircuitBreakerRegistry.of(configs)
    }
}