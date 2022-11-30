package com.global.payment.application.gateway.r2dbc.integration

import com.global.payment.application.gateway.r2dbc.entities.MERCHANT_SUBSCRIPTION_TABLE_NAME
import com.global.payment.application.gateway.r2dbc.entities.MerchantEntity
import io.micronaut.data.annotation.Query
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.r2dbc.annotation.R2dbcRepository
import io.micronaut.data.repository.kotlin.CoroutineCrudRepository
import java.util.UUID
import javax.transaction.Transactional

@R2dbcRepository(dialect = Dialect.POSTGRES)
@Transactional(value = Transactional.TxType.MANDATORY)
interface MerchantRepository: CoroutineCrudRepository<MerchantEntity, UUID>{
    @Query(
        """
            INSERT INTO $MERCHANT_SUBSCRIPTION_TABLE_NAME (app_id, merchant_id)
            VALUES (:appId, :merchantId)
        """
    )
    suspend fun addAppToMerchant(appId: UUID, merchantId: UUID): Long
}
