package com.global.payment.application.gateway.r2dbc.integration

import com.global.payment.application.gateway.r2dbc.entities.AppEntity
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.r2dbc.annotation.R2dbcRepository
import io.micronaut.data.repository.reactive.ReactiveStreamsCrudRepository
import io.micronaut.transaction.TransactionDefinition
import io.micronaut.transaction.annotation.Transactional
import java.util.UUID

@R2dbcRepository(dialect = Dialect.POSTGRES)
@Transactional(propagation = TransactionDefinition.Propagation.MANDATORY)
interface AppRepository : ReactiveStreamsCrudRepository<AppEntity, UUID>
