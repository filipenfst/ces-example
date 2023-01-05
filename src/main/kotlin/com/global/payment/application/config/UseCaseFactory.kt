package com.global.payment.application.config

import com.global.payment.application.gateway.UserGatewayProxy
import com.global.payment.domain.user.services.UserAppAccessPort
import com.global.payment.usecase.CheckUserAppAccessUseCase
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class UseCaseFactory {
    @Bean
    fun checkUserAppAccessUseCase(
        userAppAccessPort: UserAppAccessPort,
        userGatewayProxy: UserGatewayProxy,
    ) = CheckUserAppAccessUseCase(
        userAppAccessPort = userAppAccessPort,
        userFinderPort = userGatewayProxy
    )
}
