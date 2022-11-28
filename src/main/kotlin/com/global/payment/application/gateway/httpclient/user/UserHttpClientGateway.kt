package com.global.payment.application.gateway.httpclient.user

import com.global.payment.application.gateway.httpclient.config.ClientConfiguration
import com.global.payment.application.gateway.httpclient.user.dto.UserResponse
import com.global.payment.domain.user.entities.User
import com.global.payment.domain.user.services.UserFinderPort
import io.github.resilience4j.kotlin.circuitbreaker.executeSuspendFunction
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.client.annotation.Client
import jakarta.inject.Named
import jakarta.inject.Singleton
import java.util.UUID


@Singleton
class UserHttpClientGateway(
    @Named("user-api") private val clientConfiguration: ClientConfiguration,
    private val userApiClient: UserApiClient,
) : UserFinderPort {
    override suspend fun findUser(id: UUID): User? = clientConfiguration.circuitBreaker.executeSuspendFunction {
        userApiClient.findUser(id.toString())?.toDomain()
    }
}

@Client("\${client.user-api.base-url}")
interface UserApiClient {
    @Get("/user/{id}")
    suspend fun findUser(@PathVariable id: String): UserResponse?
}