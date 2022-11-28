package com.global.payment.application.gateway.r2dbc.integration

import com.global.payment.application.gateway.r2dbc.entities.AppEntity
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.r2dbc.annotation.R2dbcRepository
import io.micronaut.data.repository.reactive.ReactiveStreamsCrudRepository
import java.util.*

@R2dbcRepository(dialect = Dialect.POSTGRES)
interface AppRepository : ReactiveStreamsCrudRepository<AppEntity, UUID>
