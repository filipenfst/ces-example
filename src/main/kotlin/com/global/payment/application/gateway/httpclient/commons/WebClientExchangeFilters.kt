package com.global.payment.application.gateway.httpclient.commons

import kotlinx.coroutines.reactor.mono
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFilterFunction

object WebClientExchangeFilters {

    private fun filter(processor: suspend (ClientRequest, ClientResponse) -> ClientResponse) =
        ExchangeFilterFunction { request, next ->
            next.exchange(request).flatMap {
                mono { processor(request, it) }
            }
        }

    fun filter(processor: suspend (WebClientResponse) -> Unit) = filter { request, response ->
        val webClientResponse = WebClientResponse.from(request, response)
        processor(webClientResponse)

        webClientResponse.body?.let { response.mutate().body(it).build() } ?: response
    }

}
