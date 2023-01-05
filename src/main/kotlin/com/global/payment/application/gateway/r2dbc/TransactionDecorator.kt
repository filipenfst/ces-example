package com.global.payment.application.gateway.r2dbc

import brave.Tracing
import brave.propagation.CurrentTraceContext
import brave.propagation.TraceContext
//import io.opentelemetry.api.trace.Span
//import io.opentelemetry.extension.kotlin.asContextElement
import kotlinx.coroutines.ThreadContextElement
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.slf4j.MDCContext
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.coroutines.CoroutineContext

//@Singleton
//open class TransactionDecorator {
//    @Transactional
//    open suspend fun <T> withTransaction(block: suspend () -> T) = withContext(coroutineContext()){ block()}
//}

@Service
class TransactionDecorator {

    @Transactional
    suspend fun <T> withTransaction(block: suspend () -> T) = withContext(
        currentCoroutineContext().plus(coroutineTXContext())
    ) {
       block()
    }

}

fun coroutineTXContext(): CoroutineContext {
    return MDCContext()
//        .plus(Span.current().asContextElement())
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
