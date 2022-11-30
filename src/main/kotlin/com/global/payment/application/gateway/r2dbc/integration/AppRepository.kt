package com.global.payment.application.gateway.r2dbc.integration

import com.global.payment.application.gateway.r2dbc.entities.AppEntity
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.r2dbc.annotation.R2dbcRepository
import io.micronaut.data.repository.kotlin.CoroutineCrudRepository
import java.util.UUID
import javax.transaction.Transactional

@R2dbcRepository(dialect = Dialect.POSTGRES)
@Transactional(value = Transactional.TxType.MANDATORY)
interface AppRepository : CoroutineCrudRepository<AppEntity, UUID>
