package com.global.payment.application.gateway.r2dbc

import brave.Tracing
import brave.propagation.CurrentTraceContext
import brave.propagation.TraceContext
import com.global.payment.commons.logger.logInfo
import io.micronaut.data.r2dbc.operations.R2dbcOperations
import io.micronaut.transaction.TransactionDefinition
import io.micronaut.transaction.support.TransactionSynchronizationManager
import io.opentelemetry.api.trace.Span
import io.opentelemetry.extension.kotlin.asContextElement
import jakarta.inject.Singleton
import kotlinx.coroutines.ThreadContextElement
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.asCoroutineContext
import kotlinx.coroutines.reactor.mono
import kotlinx.coroutines.slf4j.MDCContext
import kotlinx.coroutines.withContext
import reactor.util.context.ContextView
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

//@Singleton
//open class TransactionDecorator {
//    @Transactional
//    open suspend fun <T> withTransaction(block: suspend () -> T) = withContext(coroutineContext()){ block()}
//}

@Singleton
open class TransactionDecorator(private val r2dbcOperations: R2dbcOperations) {

    open suspend fun <T> withTransaction(block: suspend () -> T) = withContext(
        currentCoroutineContext().plus(coroutineTXContext())
    ) {
        logInfo("Starting transaction")
        r2dbcOperations.withSuspendingTransaction(block = block)
    }

}

private suspend fun <T> R2dbcOperations.withSuspendingTransaction(
    definition: TransactionDefinition = TransactionDefinition.DEFAULT,
    block: suspend () -> T
): T = withTransaction(definition) {
    mono(coroutineTXContext()) {
        block()
    }
}.awaitSingle()


fun coroutineTXContext(): CoroutineContext {
    return TransactionSynchronizationManager.getResource(ContextView::class.java)
        ?.let { it as ContextView }
        .let { it?.asCoroutineContext() ?: EmptyCoroutineContext }
        .plus(MDCContext())
        .plus(Span.current().asContextElement())
        .plus(TracingThreadContextElement())
}
class TracingThreadContextElement(
    private val traceContext: TraceContext? = currentTraceContext()?.get(),
) : ThreadContextElement<CurrentTraceContext.Scope?> {
    override val key: CoroutineContext.Key<*>
        get() = KEY

    override fun updateThreadContext(context: CoroutineContext): CurrentTraceContext.Scope? {
        return currentTraceContext()?.maybeScope(traceContext)
    }

    override fun restoreThreadContext(context: CoroutineContext, oldState: CurrentTraceContext.Scope?) {
        oldState?.close()
    }

    companion object {
        val KEY = object : CoroutineContext.Key<TracingThreadContextElement> {}

        fun currentTraceContext(): CurrentTraceContext? {
            return Tracing.current()?.currentTraceContext()
        }
    }
}
