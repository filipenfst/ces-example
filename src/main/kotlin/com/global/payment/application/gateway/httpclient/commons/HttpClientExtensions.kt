package com.global.payment.application.gateway.httpclient.commons

import com.global.payment.commons.logger.logError
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.exceptions.HttpClientResponseException
import kotlinx.coroutines.reactive.awaitSingle

suspend inline fun <reified T> HttpClient.awaitRetrieve(
    request: HttpRequest<Any>,
    onError: (HttpClientResponseException) -> T = { throw it }
): T = try {
    retrieve(request, T::class.java).awaitSingle()
} catch (ex: HttpClientResponseException) {
    logError("Http error when retrieving data: $ex", ex = ex)
    onError.invoke(ex)
}
