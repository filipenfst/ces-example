package com.global.payment.application.gateway.httpclient.commons

import org.springframework.http.ReactiveHttpOutputMessage
import org.springframework.web.reactive.function.BodyInserter
import org.springframework.web.reactive.function.BodyInserters
import reactor.core.publisher.Mono

class BodyLoggingInserter<T : Any> private constructor(
    private val wrappedBodyInserter: BodyInserter<T, ReactiveHttpOutputMessage>,
    body: T
) : BodyInserter<T, ReactiveHttpOutputMessage> {
    val bodyJsonString: String = ObjectMapperConfig.mapper().writeValueAsString(body)

    companion object {
        fun <T : Any> fromValue(body: T): BodyInserter<T, ReactiveHttpOutputMessage> {
            val bodyInserter = BodyInserters.fromValue(body)
            return BodyLoggingInserter(bodyInserter, body)
        }
    }

    override fun insert(outputMessage: ReactiveHttpOutputMessage, context: BodyInserter.Context): Mono<Void> {
        return wrappedBodyInserter.insert(outputMessage, context)
    }
}


fun <T : Any> T.toLoggableBody() = BodyLoggingInserter.fromValue(this)
