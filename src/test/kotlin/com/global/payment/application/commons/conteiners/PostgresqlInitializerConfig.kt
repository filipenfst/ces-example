package com.global.payment.application.commons.conteiners

import com.global.payment.commons.logger.logInfo
import io.debezium.config.Configuration
import io.debezium.embedded.EmbeddedEngine
import io.debezium.engine.DebeziumEngine
import io.debezium.engine.format.Json
import io.micronaut.data.r2dbc.operations.R2dbcOperations
import io.r2dbc.spi.Result
import io.r2dbc.spi.Row
import io.r2dbc.spi.RowMetadata
import kotlinx.coroutines.GlobalScope
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
import java.util.Properties
import java.util.concurrent.Executors


object PostgresContainer : PostgreSQLContainer<PostgresContainer>(
    DockerImageName.parse("debezium/postgres:15-alpine").asCompatibleSubstituteFor("postgres")
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
        startDebezium()
        it.values.forEach { v -> logInfo("Postgresql----------$v") }
    }

    private fun startDebezium() {
        val props: Properties = Configuration.empty()
//            .withSystemProperties(Function.identity())
            .edit()
            .with(EmbeddedEngine.CONNECTOR_CLASS, "io.debezium.connector.postgresql.PostgresConnector")
            .with(EmbeddedEngine.ENGINE_NAME, "test-connector")
            // for demo purposes let's store offsets and history only in memory
            .with(EmbeddedEngine.OFFSET_STORAGE, "org.apache.kafka.connect.storage.MemoryOffsetBackingStore")

            .with("database.hostname", container.host)
            .with("database.port", container.firstMappedPort)
            .with("database.user", container.username)
            .with("database.password", container.password)
            .with("database.dbname", container.databaseName)
            .with("table.include.list", "public.users")
            .with("topic.prefix", "test-connector")
            .with("slot.name", "test_connector_slot")
            .with("schema.history.internal", "io.debezium.storage.file.history.FileSchemaHistory")
            .with("schemas.enable", false)
            .build().asProperties()

        DebeziumEngine.create(Json::class.java)
            .using(props)
            .notifying { record ->
                logInfo(msg = "aaaaaaaaaaaaaaaaaaaaaaaaaa $record")
            }
            .build().also { engine ->
                Executors.newSingleThreadExecutor().execute(engine)
            }
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
