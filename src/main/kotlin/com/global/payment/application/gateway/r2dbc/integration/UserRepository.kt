package com.global.payment.application.gateway.r2dbc.integration

import com.global.payment.application.gateway.r2dbc.entities.APP_TABLE_NAME
import com.global.payment.application.gateway.r2dbc.entities.MERCHANT_SUBSCRIPTION_TABLE_NAME
import com.global.payment.application.gateway.r2dbc.entities.MERCHANT_TABLE_NAME
import com.global.payment.application.gateway.r2dbc.entities.MERCHANT_USERS_TABLE_NAME
import com.global.payment.application.gateway.r2dbc.entities.USER_TABLE_NAME
import com.global.payment.application.gateway.r2dbc.entities.UserEntity
import io.micronaut.data.annotation.Query
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.r2dbc.annotation.R2dbcRepository
import io.micronaut.data.repository.reactive.ReactiveStreamsCrudRepository
import io.micronaut.transaction.TransactionDefinition
import io.micronaut.transaction.annotation.Transactional
import java.util.UUID
import org.reactivestreams.Publisher

@R2dbcRepository(dialect = Dialect.POSTGRES)
@Transactional(propagation = TransactionDefinition.Propagation.MANDATORY)
interface UserRepository : ReactiveStreamsCrudRepository<UserEntity, UUID> {

    @Query(
            """
        SELECT u.* FROM $USER_TABLE_NAME u 
            INNER JOIN $MERCHANT_USERS_TABLE_NAME mu ON u.id = mu.user_id
            INNER JOIN $MERCHANT_TABLE_NAME m ON m.id = mu.merchant_id
            WHERE m.mid = :mid
            """
    )
    fun findByMID(mid: String): Publisher<UserEntity>

    @Query(
            """
        SELECT EXISTS (SELECT u.* FROM $USER_TABLE_NAME u 
            INNER JOIN $MERCHANT_USERS_TABLE_NAME mu ON u.id = mu.user_id
            INNER JOIN $MERCHANT_SUBSCRIPTION_TABLE_NAME ms ON ms.merchant_id = mu.merchant_id
            INNER JOIN $APP_TABLE_NAME a ON a.id = ms.app_id
            WHERE u.id = :userId AND a.application_id = :applicationId)::boolean
            """
    )
    fun hasAccess(userId: UUID, applicationId: String): Publisher<Boolean>

    @Query(
            """
            INSERT INTO $MERCHANT_USERS_TABLE_NAME (user_id, merchant_id)
            VALUES (:userId, :merchantId)
        """
    )
    fun addUserToMerchant(userId: UUID, merchantId: UUID): Publisher<Long>
}
