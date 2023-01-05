package com.global.payment.application.gateway

import com.global.payment.application.gateway.httpclient.user.UserHttpClientGateway
import com.global.payment.application.gateway.r2dbc.UsersR2DBCGateway
import com.global.payment.domain.user.entities.User
import com.global.payment.domain.user.services.UserFinderPort
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UserGatewayProxy(
    private val userHttpClientGateway: UserHttpClientGateway,
    private val usersR2DBCGateway: UsersR2DBCGateway,
) : UserFinderPort {
    override suspend fun findUser(id: UUID): User? {
        return usersR2DBCGateway.findUser(id) ?: userHttpClientGateway.findUser(id)
            ?.also {
                usersR2DBCGateway.save(it)
            }
    }
}
