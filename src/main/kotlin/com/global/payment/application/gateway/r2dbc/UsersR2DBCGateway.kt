package com.global.payment.application.gateway.r2dbc

import com.global.payment.application.gateway.r2dbc.entities.toDomain
import com.global.payment.application.gateway.r2dbc.entities.toEntity
import com.global.payment.application.gateway.r2dbc.integration.UserRepository
import com.global.payment.domain.user.entities.User
import com.global.payment.domain.user.services.UserAppAccessPort
import com.global.payment.domain.user.services.UserFinderPort
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import java.util.UUID

@Singleton
class UsersR2DBCGateway(
    private val userRepository: UserRepository
) : UserAppAccessPort, UserFinderPort {

    fun listUsersForMerchantByMid(mid: String): Flow<User> {
        return userRepository.findByMID(mid = mid).toDomain()
    }

    override suspend fun hasAccessToApp(userId: UUID, applicationId: String): Boolean {
        return userRepository.hasAccess(userId = userId, applicationId = applicationId).awaitSingle()
    }

    override suspend fun findUser(id: UUID): User? {
        return userRepository.findById(id).awaitFirstOrNull()?.toDomain()
    }

    suspend fun save(user: User): User {
        return userRepository.save(user.toEntity()).awaitSingle().toDomain()
    }
}
