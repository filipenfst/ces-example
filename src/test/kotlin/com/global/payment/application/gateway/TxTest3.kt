package com.global.payment.application.gateway

import com.global.payment.application.IntegrationTests
import com.global.payment.application.gateway.r2dbc.withFlowTransaction
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.r2dbc.annotation.R2dbcRepository
import io.micronaut.data.r2dbc.operations.R2dbcOperations
import io.micronaut.data.repository.kotlin.CoroutineCrudRepository
import io.micronaut.data.repository.reactive.ReactorCrudRepository
import io.micronaut.transaction.TransactionDefinition
import io.micronaut.transaction.annotation.Transactional
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.mono
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono


@MappedEntity
data class Record(
    @field:Id val id: UUID,
    val foo: UUID,
    val bar: UUID
)

@R2dbcRepository(dialect = Dialect.POSTGRES)
interface RecordCoroutineRepository : CoroutineCrudRepository<Record, UUID> {
    suspend fun existsByFoo(foo: UUID): Boolean?
    suspend fun existsByBar(bar: UUID): Boolean
}

@R2dbcRepository(dialect = Dialect.POSTGRES)
interface RecordReactiveRepository : ReactorCrudRepository<Record, UUID> {
    fun existsByFoo(foo: UUID): Mono<Boolean?>
    fun existsByBar(bar: UUID): Mono<Boolean>
}


class TxTest3 : IntegrationTests {
    @Inject
    lateinit var recordCoroutineRepository: RecordCoroutineRepository

    @Inject
    lateinit var recordTransactionalService: RecordTransactionalService

    @Inject
    lateinit var recordDeclarativeTransactionalService: RecordDeclarativeTransactionalService

    // Pending feature: Flow doesn't propagate Reactor context
    @Test
    fun `coroutines returning flow inside transaction`(): Unit = runBlocking {
        val records = (1..2).map { Record(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()) }

        recordTransactionalService.saveAllUsingCoroutines(records).collect { }

        Assertions.assertEquals(true, recordCoroutineRepository.existsByFoo(records[0].foo))
        Assertions.assertEquals(true, recordCoroutineRepository.existsByBar(records[0].bar))
        Assertions.assertEquals(true, recordCoroutineRepository.existsByFoo(records[1].foo))
        Assertions.assertEquals(true, recordCoroutineRepository.existsByBar(records[1].bar))
    }

    // Pending feature: Flow doesn't propagate Reactor context
    @Test
    fun `reactive streams returning flow inside transaction`(): Unit = runBlocking {
        val records = (1..2).map { Record(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()) }

        recordTransactionalService.saveAllUsingReactiveStreams(records).collect { }

        Assertions.assertEquals(true, recordCoroutineRepository.existsByFoo(records[0].foo))
        Assertions.assertEquals(true, recordCoroutineRepository.existsByBar(records[0].bar))
        Assertions.assertEquals(true, recordCoroutineRepository.existsByFoo(records[1].foo))
        Assertions.assertEquals(true, recordCoroutineRepository.existsByBar(records[1].bar))
    }

    @Test
    fun `coroutines suspending inside transaction`(): Unit = runBlocking {
        val records = (1..2).map { Record(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()) }

        recordTransactionalService.saveAllSuspendingUsingCoroutines(records)

        Assertions.assertEquals(true, recordCoroutineRepository.existsByFoo(records[0].foo))
        Assertions.assertEquals(true, recordCoroutineRepository.existsByBar(records[0].bar))
        Assertions.assertEquals(true, recordCoroutineRepository.existsByFoo(records[1].foo))
        Assertions.assertEquals(true, recordCoroutineRepository.existsByBar(records[1].bar))
    }

    // Pending feature: Flow doesn't propagate Reactor context
    @Test
    fun `reactive streams suspending inside transaction`(): Unit = runBlocking {
        val records = (1..2).map { Record(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()) }

        recordTransactionalService.saveAllSuspendingUsingReactiveStreams(records)

        Assertions.assertEquals(true, recordCoroutineRepository.existsByFoo(records[0].foo))
        Assertions.assertEquals(true, recordCoroutineRepository.existsByBar(records[0].bar))
        Assertions.assertEquals(true, recordCoroutineRepository.existsByFoo(records[1].foo))
        Assertions.assertEquals(true, recordCoroutineRepository.existsByBar(records[1].bar))
    }


    // Pending feature: Flow doesn't propagate Reactor context
    @Test
    fun `coroutines returning flow inside declarative transaction`(): Unit = runBlocking {
        val records = (1..2).map { Record(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()) }

        recordDeclarativeTransactionalService.saveAllUsingCoroutines(records).collect { }

        Assertions.assertEquals(true, recordCoroutineRepository.existsByFoo(records[0].foo))
        Assertions.assertEquals(true, recordCoroutineRepository.existsByBar(records[0].bar))
        Assertions.assertEquals(true, recordCoroutineRepository.existsByFoo(records[1].foo))
        Assertions.assertEquals(true, recordCoroutineRepository.existsByBar(records[1].bar))
    }

    @Test
    fun `reactive streams returning flow inside declarative transaction`(): Unit = runBlocking {
        val records = (1..2).map { Record(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()) }

        recordDeclarativeTransactionalService.saveAllUsingReactiveStreams(records).collect { }

        Assertions.assertEquals(true, recordCoroutineRepository.existsByFoo(records[0].foo))
        Assertions.assertEquals(true, recordCoroutineRepository.existsByBar(records[0].bar))
        Assertions.assertEquals(true, recordCoroutineRepository.existsByFoo(records[1].foo))
        Assertions.assertEquals(true, recordCoroutineRepository.existsByBar(records[1].bar))
    }

    @Test
    fun `coroutines suspending inside declarative transaction`(): Unit = runBlocking {
        val records = (1..2).map { Record(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()) }

        recordDeclarativeTransactionalService.saveAllSuspendingUsingCoroutines(records)

        Assertions.assertEquals(true, recordCoroutineRepository.existsByFoo(records[0].foo))
        Assertions.assertEquals(true, recordCoroutineRepository.existsByBar(records[0].bar))
        Assertions.assertEquals(true, recordCoroutineRepository.existsByFoo(records[1].foo))
        Assertions.assertEquals(true, recordCoroutineRepository.existsByBar(records[1].bar))
    }

    @Test
    fun `reactive streams suspending inside declarative transaction`(): Unit = runBlocking {
        val records = (1..2).map { Record(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()) }

        recordDeclarativeTransactionalService.saveAllSuspendingUsingReactiveStreams(records)

        Assertions.assertEquals(true, recordCoroutineRepository.existsByFoo(records[0].foo))
        Assertions.assertEquals(true, recordCoroutineRepository.existsByBar(records[0].bar))
        Assertions.assertEquals(true, recordCoroutineRepository.existsByFoo(records[1].foo))
        Assertions.assertEquals(true, recordCoroutineRepository.existsByBar(records[1].bar))
    }

}


@Transactional(propagation = TransactionDefinition.Propagation.MANDATORY)
@R2dbcRepository(dialect = Dialect.POSTGRES)
interface RecordTransactionalCoroutineRepository : CoroutineCrudRepository<Record, UUID>

@Transactional(propagation = TransactionDefinition.Propagation.MANDATORY)
@R2dbcRepository(dialect = Dialect.POSTGRES)
interface RecordTransactionalReactiveStreamsRepository : ReactorCrudRepository<Record, UUID>

@Singleton
@Transactional
open class RecordTransactionalService(
    private val coroutineRepository: RecordTransactionalCoroutineRepository,
    private val reactiveStreamsRepository: RecordTransactionalReactiveStreamsRepository,
) {
    open suspend fun saveAllUsingCoroutines(records: Iterable<Record>): Flow<Record> =
        coroutineRepository.saveAll(records)

    open fun saveAllUsingReactiveStreams(records: Iterable<Record>): Flow<Record> =
        reactiveStreamsRepository.saveAll(records).asFlow()

    open suspend fun saveAllSuspendingUsingCoroutines(records: Iterable<Record>): List<Record> = records.map {
        coroutineRepository.save(it)
    }

    open suspend fun saveAllSuspendingUsingReactiveStreams(records: Iterable<Record>): List<Record> =
//        withContext(coroutineTXContext()) {
        records.map {
            reactiveStreamsRepository.save(it).awaitSingle()
        }
//        }
}


@Singleton
open class RecordDeclarativeTransactionalService(
    private val coroutineRepository: RecordTransactionalCoroutineRepository,
    private val reactiveStreamsRepository: RecordTransactionalReactiveStreamsRepository,
    private val r2dbcOperations: R2dbcOperations
) {
    open fun saveAllUsingCoroutines(records: Iterable<Record>): Flow<Record> = r2dbcOperations.withFlowTransaction {
        coroutineRepository.saveAll(records)
    }

    open fun saveAllUsingReactiveStreams(records: Iterable<Record>): Flow<Record> =
        r2dbcOperations.withFlowTransaction {
            reactiveStreamsRepository.saveAll(records).asFlow()
        }

    open suspend fun saveAllSuspendingUsingCoroutines(records: Iterable<Record>): List<Record> {
        return r2dbcOperations.withTransaction {
            mono {
                records.map {
                    coroutineRepository.save(it)
                }
            }
        }.awaitSingle()
    }

    open suspend fun saveAllSuspendingUsingReactiveStreams(records: Iterable<Record>): List<Record> {
        return r2dbcOperations.withTransaction {
            mono {
                records.map {
                    reactiveStreamsRepository.save(it).awaitSingle()
                }
            }
        }.awaitSingle()
    }
}
