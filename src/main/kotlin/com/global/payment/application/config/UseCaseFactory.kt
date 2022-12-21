package com.global.payment.application.config

import com.global.payment.application.gateway.UserGatewayProxy
import com.global.payment.domain.user.services.UserAppAccessPort
import com.global.payment.domain.user.services.UserPublisherPort
import com.global.payment.usecase.CheckUserAppAccessUseCase
import io.micronaut.context.annotation.Factory
import jakarta.inject.Singleton


@Factory
class UseCaseFactory {
    @Singleton
    fun checkUserAppAccessUseCase(
        userAppAccessPort: UserAppAccessPort,
        userGatewayProxy: UserGatewayProxy,
        userPublisherPort: UserPublisherPort
    ) = CheckUserAppAccessUseCase(
        userAppAccessPort = userAppAccessPort,
        userFinderPort = userGatewayProxy,
        userPublisherPort = userPublisherPort
    )
}
