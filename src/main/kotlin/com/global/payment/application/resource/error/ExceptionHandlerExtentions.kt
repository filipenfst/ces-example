package com.global.payment.application.resource.error

import com.global.payment.commons.logger.logError
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyValueAndAwait

suspend fun <E : Throwable> E.handle(
    request: ServerRequest,
    serverResponse: suspend () -> Pair<HttpStatus, ErrorResponseDTO>
) = this.log(request).run { serverResponse() }.log(request)

private suspend fun <E : Throwable> E.log(request: ServerRequest): E {
    logError(
        msg = "Handling error on requestMethod: ${request.method()}, " +
            "request: ${request.uri()}, " +
            "body: ${request.bodyToMono(String::class.java).awaitSingleOrNull()}, " +
            "message: $message,  " +
            "exception: ${javaClass.simpleName}",
    )
    return this
}

private suspend fun Pair<HttpStatus, ErrorResponseDTO>.log(request: ServerRequest): ServerResponse {
    logError(
        msg = (
            "Responding requestMethod: ${request.method()}, " +
                "request: ${request.uri()}, " +
                "requestBody: ${request.bodyToMono(String::class.java).awaitSingleOrNull()}, " +
                "response: $this"
            )
    )
    return ServerResponse.status(first).bodyValueAndAwait(second)
}
