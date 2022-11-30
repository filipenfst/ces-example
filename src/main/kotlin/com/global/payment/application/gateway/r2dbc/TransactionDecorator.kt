package com.global.payment.application.gateway.r2dbc

import com.global.payment.commons.logger.logInfo
import io.micronaut.data.r2dbc.operations.R2dbcOperations
import jakarta.inject.Singleton
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.asCoroutineContext
import kotlinx.coroutines.reactor.mono
import reactor.core.publisher.Flux
import reactor.util.context.ContextView
import javax.transaction.Transactional
import kotlin.coroutines.EmptyCoroutineContext

//@Singleton
//@Transactional
//open class TransactionDecorator {
//    open suspend fun <T> withTransaction(block: suspend () -> T): T = coroutineScope {
//        printContext()
//        println(this.coroutineContext)
//        block().also {
//            printContext()
//        }
//    }
//}

@Singleton
@Transactional
open class TransactionDecorator(private val r2dbcOperations: R2dbcOperations) {
    open suspend fun <T> withTransaction(block: suspend () -> T) = r2dbcOperations.withSuspendingTransaction(block)

}

private suspend fun <T> R2dbcOperations.withSuspendingTransaction(
    block: suspend () -> T
): T = withTransaction {
    mono {
        printContext()
        block().also {
            printContext()
        }
    }
}.awaitSingle().also {
    printContext()
}

suspend fun coroutineContext() = context()?.asCoroutineContext() ?: EmptyCoroutineContext

suspend fun printContext() = Flux.deferContextual { context ->
    mono {
        logInfo(
            "Transaction on context:${
                context.getOrDefault<Any>(
                    "io.micronaut.tx.status.r2dbc.default",
                    null
                ) != null
            }"
        )
    }
}.awaitSingle()

suspend fun context(): ContextView? = Flux.deferContextual { mono { it } }.awaitSingle()