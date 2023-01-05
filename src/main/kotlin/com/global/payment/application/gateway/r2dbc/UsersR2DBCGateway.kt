package com.global.payment.application.gateway.r2dbc

import com.global.payment.application.gateway.r2dbc.entities.toDomain
import com.global.payment.application.gateway.r2dbc.entities.toEntity
import com.global.payment.application.gateway.r2dbc.integration.UserRepository
import com.global.payment.commons.logger.logInfo
import com.global.payment.domain.user.entities.User
import com.global.payment.domain.user.services.UserAppAccessPort
import com.global.payment.domain.user.services.UserFinderPort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UsersR2DBCGateway(
    private val userRepository: UserRepository
) : UserAppAccessPort, UserFinderPort {

    fun listUsersForMerchantByMid(mid: String): Flow<User> {
        logInfo(msg = "Listing merchants from db by mid: $mid")
        return userRepository.findByMID(mid = mid).toDomain()
    }

    override suspend fun hasAccessToApp(userId: UUID, applicationId: String): Boolean {
        logInfo(msg = "Checking access to app on db for user:$userId and app:$applicationId")
        return userRepository.hasAccess(userId = userId, applicationId = applicationId).awaitSingle()
    }

    override suspend fun findUser(id: UUID): User? {
        logInfo(msg = "Searching user on db by id: $id")
        return userRepository.findByExternalId(id)?.toDomain().also {
            logInfo(msg = "User found: ${it != null}")
        }
    }

    suspend fun save(user: User): User {
        logInfo(msg = "Saving on db user: $user")
        return userRepository.save(user.toEntity()).awaitSingle().toDomain().also {
            logInfo(msg = "User saved")
        }
    }
}
