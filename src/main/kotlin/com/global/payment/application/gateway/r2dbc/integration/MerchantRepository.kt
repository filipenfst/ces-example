package com.global.payment.application.gateway.r2dbc.integration

import com.global.payment.application.gateway.r2dbc.entities.MERCHANT_SUBSCRIPTION_TABLE_NAME
import com.global.payment.application.gateway.r2dbc.entities.MerchantEntity
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Repository
@Transactional(propagation = Propagation.MANDATORY)
interface MerchantRepository: ReactiveCrudRepository<MerchantEntity, UUID>{
    @Query(
        """
            INSERT INTO $MERCHANT_SUBSCRIPTION_TABLE_NAME (app_id, merchant_id)
            VALUES (:appId, :merchantId)
        """
    )
    suspend fun addAppToMerchant(appId: Int, merchantId: Int)
}
