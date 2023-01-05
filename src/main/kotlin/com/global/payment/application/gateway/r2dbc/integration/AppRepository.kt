package com.global.payment.application.gateway.r2dbc.integration

import com.global.payment.application.gateway.r2dbc.entities.AppEntity
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Repository
@Transactional(propagation = Propagation.MANDATORY)
interface AppRepository : ReactiveCrudRepository<AppEntity, UUID>
