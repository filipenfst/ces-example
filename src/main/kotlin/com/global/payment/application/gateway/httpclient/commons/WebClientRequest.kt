package com.global.payment.application.gateway.httpclient.commons

import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatusCode
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse

data class WebClientRequest(
    val method: HttpMethod,
    val url: String,
    val body: String
) {
    constructor(clientRequest: ClientRequest) : this(
        method = clientRequest.method(),
        url = clientRequest.url().toString(),
        body = clientRequest.bodyString()
    )

    companion object {
        private fun ClientRequest.bodyString() = when (val body = body()) {
            is BodyLoggingInserter -> body.bodyJsonString
            else -> ""
        }
    }
}

data class WebClientResponse(
    val request: WebClientRequest,
    val statusCode: HttpStatusCode,
    val body: String?
) {

    companion object {

        private suspend fun ClientResponse.body() = bodyToMono(String::class.java).awaitSingleOrNull() ?: ""

        suspend fun from(
            clientRequest: ClientRequest,
            clientResponse: ClientResponse
        ) = WebClientResponse(
            request = WebClientRequest(clientRequest),
            statusCode = clientResponse.statusCode(),
            body = clientResponse.body()
        )
    }
}
