package com.global.payment.application.gateway.r2dbc

import io.micronaut.data.r2dbc.operations.R2dbcOperations
import jakarta.inject.Singleton
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.mono

//@Singleton
//open class TransactionDecorator {
//    @Transactional
//    open suspend fun <T> withTransaction(block: suspend () -> T) = block()
//}

@Singleton
open class TransactionDecorator(private val r2dbcOperations: R2dbcOperations) {
    open suspend fun <T> withTransaction(block: suspend () -> T) = r2dbcOperations.withSuspendingTransaction(block)
}

private suspend fun <T> R2dbcOperations.withSuspendingTransaction(
    block: suspend () -> T
): T = withTransaction {
    mono {
        block()
    }
}.awaitSingle()
