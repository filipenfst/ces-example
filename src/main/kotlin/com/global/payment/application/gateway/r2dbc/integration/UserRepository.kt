package com.global.payment.application.gateway.r2dbc.integration

import com.global.payment.application.gateway.r2dbc.entities.APP_TABLE_NAME
import com.global.payment.application.gateway.r2dbc.entities.MERCHANT_SUBSCRIPTION_TABLE_NAME
import com.global.payment.application.gateway.r2dbc.entities.MERCHANT_TABLE_NAME
import com.global.payment.application.gateway.r2dbc.entities.MERCHANT_USERS_TABLE_NAME
import com.global.payment.application.gateway.r2dbc.entities.USER_TABLE_NAME
import com.global.payment.application.gateway.r2dbc.entities.UserEntity
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono
import java.util.UUID

@Repository
@Transactional(propagation = Propagation.MANDATORY)
interface UserRepository : ReactiveCrudRepository<UserEntity, Int> {

    @Query(
        """
        SELECT u.* FROM $USER_TABLE_NAME u 
            INNER JOIN $MERCHANT_USERS_TABLE_NAME mu ON u.id = mu.user_id
            INNER JOIN $MERCHANT_TABLE_NAME m ON m.id = mu.merchant_id
            WHERE m.mid = :mid
            """
    )
    fun findByMID(mid: String): Mono<UserEntity>

    @Query(
        """
        SELECT EXISTS (SELECT u.* FROM $USER_TABLE_NAME u 
            INNER JOIN $MERCHANT_USERS_TABLE_NAME mu ON u.id = mu.user_id
            INNER JOIN $MERCHANT_SUBSCRIPTION_TABLE_NAME ms ON ms.merchant_id = mu.merchant_id
            INNER JOIN $APP_TABLE_NAME a ON a.id = ms.app_id
            WHERE u.external_id = :userId AND a.application_id = :applicationId)::boolean
            """
    )
    fun hasAccess(userId: UUID, applicationId: String): Mono<Boolean>

    @Query(
        """
            INSERT INTO $MERCHANT_USERS_TABLE_NAME (user_id, merchant_id)
            VALUES (:userId, :merchantId)
        """
    )
    suspend fun addUserToMerchant(userId: Int, merchantId: Int)

    suspend fun findByExternalId(externalId: UUID): UserEntity?
}
