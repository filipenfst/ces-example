package com.global.payment.application.gateway.httpclient.commons.client

import com.global.payment.commons.logger.logInfo
import com.global.payment.application.gateway.httpclient.commons.BodyLoggingInserter
import com.global.payment.application.gateway.httpclient.commons.WebClientExchangeFilters
import io.netty.channel.ChannelOption
import kotlinx.coroutines.reactor.mono
import kotlinx.coroutines.slf4j.MDCContext
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.http.codec.ClientCodecConfigurer
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.support.WebClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory
import reactor.netty.http.client.HttpClient
import java.time.Duration


fun ClientConfiguration<*>.createWebClient(
    builderFunction: WebClient.Builder.() -> WebClient.Builder = { this }
) = WebClientDefinition(this, builderFunction)

class WebClientDefinition(
    clientConfiguration: ClientConfiguration<*>,
    builderFunction: WebClient.Builder.() -> WebClient.Builder = { this },
) {
    private val strategies = ExchangeStrategies.builder()
        .codecs { codecs: ClientCodecConfigurer ->
            codecs.defaultCodecs()
        }
        .build()
    val webClient = WebClient.builder()
        .exchangeStrategies(strategies)
        .clientConnector(ReactorClientHttpConnector(getHttpClient(clientConfiguration)))
        .baseUrl(clientConfiguration.baseUrl)
        .filter(setTraceHeaders())
        .filter(logRequest())
        .filter(logResponse())
        .builderFunction()
        .build()

    private fun getHttpClient(clientConfiguration: ClientConfiguration<*>) = HttpClient.newConnection()
        .responseTimeout(Duration.ofMillis(clientConfiguration.responseTimeout))
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, clientConfiguration.connectionTimeout)

    private fun logRequest() = ExchangeFilterFunction.ofRequestProcessor {
        val msg = "Sending request url:[${it.method()}] ${it.url()}"

        when (val body = it.body()) {
            is BodyLoggingInserter -> logInfo(msg = "$msg body:${body.bodyJsonString}")
            else -> logInfo(msg = msg)
        }

        mono { it }
    }

    private fun logResponse() = WebClientExchangeFilters.filter { response ->
        with(response) {
            val msg = "Receiving response status:$statusCode url:[${request.method}] ${request.url}"
            logInfo(msg = "$msg body:$body")
        }
    }

    private fun setTraceHeaders(): ExchangeFilterFunction {
        return ExchangeFilterFunction.ofRequestProcessor {
            mono {
                logInfo(msg = "Setting  trace headers")
                MDCContext().contextMap?.run {
                    ClientRequest.from(it)
                        .header("X-B3-TraceId", get("traceId"))
                        .header("X-B3-SpanId", get("spanId"))
                        .build()
                } ?: it
            }
        }
    }


}

inline fun <reified T> WebClientDefinition.postClient(): T {
    val httpServiceProxyFactory = HttpServiceProxyFactory.builder(WebClientAdapter.forClient(webClient))
        .build()
    return httpServiceProxyFactory.createClient(T::class.java)
}
