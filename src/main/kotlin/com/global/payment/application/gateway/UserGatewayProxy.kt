package com.global.payment.application.gateway

import com.global.payment.application.gateway.httpclient.user.UserHttpClientGateway
import com.global.payment.application.gateway.r2dbc.UsersR2DBCGateway
import com.global.payment.domain.user.entities.User
import com.global.payment.domain.user.services.UserFinderPort
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.util.UUID

@Singleton
class UserGatewayProxy(
    @Inject private val userHttpClientGateway: UserHttpClientGateway,
    @Inject private val usersR2DBCGateway: UsersR2DBCGateway,
) : UserFinderPort {
    override suspend fun findUser(id: UUID): User? {
        return usersR2DBCGateway.findUser(id) ?: userHttpClientGateway.findUser(id)
            ?.also {
                usersR2DBCGateway.save(it)
            }
    }
}
