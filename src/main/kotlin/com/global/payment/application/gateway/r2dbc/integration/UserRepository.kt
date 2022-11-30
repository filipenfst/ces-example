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
import io.micronaut.data.repository.kotlin.CoroutineCrudRepository
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.transaction.Transactional

@R2dbcRepository(dialect = Dialect.POSTGRES)
@Transactional(value = Transactional.TxType.MANDATORY)
interface UserRepository : CoroutineCrudRepository<UserEntity, UUID> {

    @Query(
        """
        SELECT u.* FROM $USER_TABLE_NAME u 
            INNER JOIN $MERCHANT_USERS_TABLE_NAME mu ON u.id = mu.user_id
            INNER JOIN $MERCHANT_TABLE_NAME m ON m.id = mu.merchant_id
            WHERE m.mid = :mid
            """
    )
    fun findAllByMID(mid: String): Flow<UserEntity>

    @Query(
        """
        SELECT EXISTS (SELECT u.* FROM $USER_TABLE_NAME u 
            INNER JOIN $MERCHANT_USERS_TABLE_NAME mu ON u.id = mu.user_id
            INNER JOIN $MERCHANT_SUBSCRIPTION_TABLE_NAME ms ON ms.merchant_id = mu.merchant_id
            INNER JOIN $APP_TABLE_NAME a ON a.id = ms.app_id
            WHERE u.id = :userId AND a.application_id = :applicationId)::boolean
            """
    )
    suspend fun hasAccess(userId: UUID, applicationId: String): Boolean

    @Query(
        """
            INSERT INTO $MERCHANT_USERS_TABLE_NAME (user_id, merchant_id)
            VALUES (:userId, :merchantId)
        """
    )
    suspend fun addUserToMerchant(userId: UUID, merchantId: UUID): Long
}
