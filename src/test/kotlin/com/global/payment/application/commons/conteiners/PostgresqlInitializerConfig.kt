package com.global.payment.application.commons.conteiners

import com.global.payment.commons.logger.logInfo
import io.micronaut.data.r2dbc.operations.R2dbcOperations
import io.r2dbc.spi.Result
import io.r2dbc.spi.Row
import io.r2dbc.spi.RowMetadata
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.asPublisher
import kotlinx.coroutines.reactive.awaitSingle
import org.reactivestreams.Publisher
import org.testcontainers.containers.Network
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName


object PostgresContainer : PostgreSQLContainer<PostgresContainer>(
    DockerImageName.parse("postgres").asCompatibleSubstituteFor("postgres")
) {
    init {
        withUsername("root")
        withPassword("root")
        withDatabaseName("test")
        withNetwork(Network.SHARED)

        start()
    }
}

internal object PostgresqlInitializerConfig : ApplicationContainerInitializerConfig<PostgresContainer> {
    override val container: PostgresContainer = PostgresContainer

    override fun getProperties() = mapOf(
        "r2dbc.datasources.default.url" to getR2dbcUrl(),
        "r2dbc.datasources.default.username" to container.username,
        "r2dbc.datasources.default.password" to container.password,

        "datasources.default.url" to container.jdbcUrl,
        "datasources.default.username" to container.username,
        "datasources.default.password" to container.password,
    ).also {
        it.values.forEach { v -> logInfo("Postgresql----------$v") }
    }

    fun getR2dbcUrl(): String =
        container.jdbcUrl.replace("jdbc", "r2dbc")
}

internal object PostgresqlInitializer : ApplicationInitializer(PostgresqlInitializerConfig)


suspend fun R2dbcOperations.resetDB() = withConnection { connection ->
    connection.createStatement(
        """
                SELECT  tablename, tableowner
                FROM pg_catalog.pg_tables
                WHERE schemaname != 'pg_catalog' AND schemaname != 'information_schema' 
                AND tablename not in ('databasechangeloglock','databasechangelog')
                """.trimIndent()
    ).execute().mapAsFlow { t, _ ->
        t["tablename"] as String
    }.filter {
        !it.contains("flyway")
    }.map {
        connection.createStatement("TRUNCATE $it CASCADE".trimIndent()).execute().awaitSingle()
    }.asPublisher()
}.asFlow().collect()

private fun <T> Flow<Flow<T>>.mergeFlows(): Flow<T> = flow {
    collect {
        emitAll(it)
    }
}

private fun <T : Any> Publisher<out Result>.mapAsFlow(
    mappingFunction: (Row, RowMetadata) -> T
) = asFlow().map { result ->
    result.map(mappingFunction).asFlow()
}.mergeFlows()
