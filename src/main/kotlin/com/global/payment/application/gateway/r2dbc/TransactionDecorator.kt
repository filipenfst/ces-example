package com.global.payment.application.gateway.r2dbc

import io.micronaut.data.r2dbc.operations.R2dbcOperations
import io.micronaut.transaction.annotation.Transactional
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.asCoroutineContext
import kotlinx.coroutines.reactor.mono
import reactor.core.publisher.Flux

@Singleton
open class TransactionDecorator {
    @Transactional
    open suspend fun <T> withTransaction(block: suspend () -> T) = block()
}

fun <T : Any> R2dbcOperations.withFlowTransaction(
    block: () -> Flow<T>
): Flow<T> = flow {
    withTransaction {
        Flux.deferContextual { c ->
            val context = c.asCoroutineContext()
            mono(context) {
                emitAll(block().flowOn(context))
            }
        }
    }.awaitSingle()
}

////@Singleton
////open class TransactionDecorator(private val r2dbcOperations: R2dbcOperations) {
////
////    open suspend fun <T> withTransaction(block: suspend () -> T) = withContext(
////        currentCoroutineContext().plus(coroutineTXContext())
////    ) {
////        logInfo("Starting transaction")
////        r2dbcOperations.withSuspendingTransaction(block = block)
////    }
////}
//
//suspend fun <T> R2dbcOperations.withSuspendingTransaction(
//    block: suspend () -> T
//): T = withTransaction {
//    mono(coroutineTXContext()) {
//        block()
//    }
//}.awaitSingle()
//
////fun <T : Any> R2dbcOperations.withFlowTransaction(
////    definition: TransactionDefinition = TransactionDefinition.DEFAULT,
////    block: () -> Flow<T>
////): Flow<T> = flow {
////    withTransaction(definition) {
////        Flux.deferContextual { c ->
////            TransactionSynchronizationManager.bindResource(ContextView::class.java, c)
////            println(c)
//////            val context = coroutineTXContext()
////            val context = c.asCoroutineContext()
////            mono(context) {
////                emitAll(block().flowOn(context))
////            }
////        }
////    }.awaitSingle()
////}
////= flow {
////
////    withSuspendingTransaction(definition) {
////        emitAll(block())
////    }
////}
//
//fun <T : Any> R2dbcOperations.withFlowTransaction(
//    definition: TransactionDefinition = TransactionDefinition.DEFAULT,
//    block: () -> Flow<T>
//): Flow<T> = flow {
//    val connection = connectionFactory().create().awaitSingle()
//    Flux.from(connection.beginTransaction()).hasElements().awaitSingle()
////    if (!resourceSupplier) throw RuntimeException("Unable to create transaction")
//    val status = DefaultReactiveTransactionStatus(definition, connection, true)
//    val context = Context.of(
//        "io.micronaut.tx.status.r2dbc.default", status,
//        "io.micronaut.tx.definition.r2dbc.default", definition
//    ).asCoroutineContext()
//    withContext(context) {
//        kotlin.runCatching {
//            block().map { emit(it) }.flowOn(context).collect()
//        }.fold({
//            Flux.from(connection.commitTransaction()).hasElements().awaitSingle()
//        }) {
//            Flux.from(connection.rollbackTransaction()).hasElements().awaitSingle()
//            throw it
//        }
//    }
//
//
////    withTransaction(definition) {
////        Flux.deferContextual { c ->
////            TransactionSynchronizationManager.bindResource(ContextView::class.java, c)
////            println(c)
//////            val context = coroutineTXContext()
////            val context = c.asCoroutineContext()
////            mono(context) {
////                emitAll(block().flowOn(context))
////            }
////        }
////    }.awaitSingle()
//}
//
//private class DefaultReactiveTransactionStatus(
//    val definition: TransactionDefinition,
//    private val connection: Connection,
//    private val isNew: Boolean
//) :
//    ReactiveTransactionStatus<Connection> {
//    private var rollbackOnly = false
//    private val completed = false
//
//    override fun getConnection(): Connection {
//        return connection
//    }
//
//    override fun isNewTransaction(): Boolean {
//        return isNew
//    }
//
//    override fun setRollbackOnly() {
//        rollbackOnly = true
//    }
//
//    override fun isRollbackOnly(): Boolean {
//        return rollbackOnly
//    }
//
//    override fun isCompleted(): Boolean {
//        return completed
//    }
//}
//
//fun coroutineTXContext(): CoroutineContext {
//    return TransactionSynchronizationManager.getResource(ContextView::class.java)!!
//        ?.let { it as ContextView }
//        .let { it?.asCoroutineContext() ?: EmptyCoroutineContext }
//        .plus(MDCContext())
//        .plus(Span.current().asContextElement())
//        .plus(TracingThreadContextElement())
//}
//
//class TracingThreadContextElement(
//    private val traceContext: TraceContext? = currentTraceContext()?.get(),
//) : ThreadContextElement<CurrentTraceContext.Scope?> {
//    override val key: CoroutineContext.Key<*>
//        get() = KEY
//
//    override fun updateThreadContext(context: CoroutineContext): CurrentTraceContext.Scope? {
//        return currentTraceContext()?.maybeScope(traceContext)
//    }
//
//    override fun restoreThreadContext(context: CoroutineContext, oldState: CurrentTraceContext.Scope?) {
//        oldState?.close()
//    }
//
//    companion object {
//        val KEY = object : CoroutineContext.Key<TracingThreadContextElement> {}
//
//        fun currentTraceContext(): CurrentTraceContext? {
//            return Tracing.current()?.currentTraceContext()
//        }
//    }
//}
