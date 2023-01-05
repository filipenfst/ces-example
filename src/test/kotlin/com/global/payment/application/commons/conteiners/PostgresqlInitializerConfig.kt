package com.global.payment.application.commons.conteiners

import com.global.payment.commons.logger.logInfo
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.awaitSingle
import org.springframework.r2dbc.core.flow
import org.testcontainers.containers.Network
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName


object PostgresContainer : PostgreSQLContainer<PostgresContainer>(
    DockerImageName.parse("debezium/postgres:13-alpine").asCompatibleSubstituteFor("postgres")
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
        "spring.r2dbc.username" to container.username,
        "spring.r2dbc.password" to container.password,
        "spring.r2dbc.url" to container.jdbcUrl.replace("jdbc", "r2dbc"),
        "spring.flyway.user" to container.username,
        "spring.flyway.password" to container.password,
        "spring.flyway.url" to container.jdbcUrl,
    ).also {
        it.values.forEach { v -> logInfo("Postgresql----------$v") }
//        runMigrations()
    }

//    private fun runMigrations() {
//        Flyway
//            .configure()
//            .dataSource(container.jdbcUrl, container.username, container.password)
//            .schemas("public")
//            .locations("classpath:db/migration", "classpath:db/scripts")
//            .load()
//            .migrate()
//    }
//
//    fun getR2dbcUrl(): String =
//        container.jdbcUrl.replace("jdbc", "r2dbc")
}

internal object PostgresqlInitializer : ApplicationInitializer(PostgresqlInitializerConfig)


suspend fun DatabaseClient.resetDB() =
    sql(
        """
                SELECT  tablename, tableowner
                FROM pg_catalog.pg_tables
                WHERE schemaname != 'pg_catalog' AND schemaname != 'information_schema' 
                AND tablename not in ('databasechangeloglock','databasechangelog')
                """.trimIndent()
    ).map { t, _ ->
        t["tablename"] as String
    }.flow().filter {
        !it.contains("flyway")
    }.map {
        sql("TRUNCATE $it CASCADE".trimIndent()).map { t, _ -> t }.awaitSingle()
    }.collect {}

//private fun <T> Flow<Flow<T>>.mergeFlows(): Flow<T> = flow {
//    collect {
//        emitAll(it)
//    }
//}
//
//private fun <T : Any> Publisher<out Result>.mapAsFlow(
//    mappingFunction: (Row, RowMetadata) -> T
//) = asFlow().map { result ->
//    result.map(mappingFunction).asFlow()
//}.mergeFlows()
