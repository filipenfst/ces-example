package com.global.payment.application.gateway.httpclient.user

import com.global.payment.application.gateway.httpclient.commons.awaitRetrieve
import com.global.payment.application.gateway.httpclient.config.ClientConfiguration
import com.global.payment.application.gateway.httpclient.user.dto.UserResponse
import com.global.payment.domain.user.entities.User
import com.global.payment.domain.user.services.UserFinderPort
import io.github.resilience4j.kotlin.circuitbreaker.executeSuspendFunction
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import jakarta.inject.Named
import jakarta.inject.Singleton
import java.util.UUID


@Singleton
class UserHttpClientGateway(
    @Named("user-api") private val clientConfiguration: ClientConfiguration,
) : UserFinderPort {
    override suspend fun findUser(id: UUID): User? = clientConfiguration.circuitBreaker.executeSuspendFunction {
        clientConfiguration.httpClient().awaitRetrieve<UserResponse?>(
            request = HttpRequest.GET("/user/$id"),
            onError = {
                if (it.status == HttpStatus.NOT_FOUND) null else throw it
            }
        )?.toDomain()
    }
}
