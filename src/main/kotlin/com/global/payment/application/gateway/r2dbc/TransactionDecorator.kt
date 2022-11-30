package com.global.payment.application.gateway.r2dbc

import jakarta.inject.Singleton
import javax.transaction.Transactional

@Singleton
open class TransactionDecorator {
    @Transactional
    open suspend fun <T> withTransaction(block: suspend () -> T) = block()
}

//@Singleton
//open class TransactionDecorator(private val r2dbcOperations:R2dbcOperations) {
//    open suspend fun <T> withTransaction(block: suspend () -> T) = r2dbcOperations.withSuspendingTransaction(block)
//
//    private suspend fun <T> R2dbcOperations.withSuspendingTransaction(
//        block: suspend () -> T
//    ): T = withTransaction {
//        publish<T> {
//            block()
//        }
//    }.awaitSingle()
//}
