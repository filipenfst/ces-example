package com.global.payment.application.gateway.httpclient.commons.client

interface ClientConfiguration<T> {
    val baseUrl: String
    val responseTimeout: Long
    val connectionTimeout: Int
//    val circuitBreaker: CircuitBreaker

    fun createClient(): T
}
