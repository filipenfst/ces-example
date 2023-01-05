package com.global.payment.application.gateway.httpclient.user

import com.global.payment.application.gateway.httpclient.user.dto.UserResponse
import com.global.payment.commons.logger.logInfo
import com.global.payment.domain.user.entities.User
import com.global.payment.domain.user.services.UserFinderPort
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import io.github.resilience4j.kotlin.circuitbreaker.executeSuspendFunction
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange
import reactor.core.publisher.Mono
import java.util.UUID


@Service
class UserHttpClientGateway(
//    private val clientConfiguration: UserApiConfiguration,
    private val userApiClient: UserApiClient,
    circuitBreakerRegistry: CircuitBreakerRegistry,
) : UserFinderPort {

    val circuitBreaker = circuitBreakerRegistry.circuitBreaker("user-api")
    override suspend fun findUser(id: UUID): User? = circuitBreaker.executeSuspendFunction {
        logInfo(msg = "Searching user api for user: $id")
        userApiClient.findUser(id.toString()).awaitSingle()?.toDomain().also {
            logInfo(msg = "User found: ${it != null}")
        }
    }
}

@HttpExchange
interface UserApiClient {
    @GetExchange("/user/{id}")
    fun findUser(@PathVariable id: String): Mono<UserResponse?>
}
