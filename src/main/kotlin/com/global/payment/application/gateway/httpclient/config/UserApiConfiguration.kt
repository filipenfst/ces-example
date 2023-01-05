package com.global.payment.application.gateway.httpclient.config

import com.global.payment.application.gateway.httpclient.commons.client.ClientConfiguration
import com.global.payment.application.gateway.httpclient.commons.client.createWebClient
import com.global.payment.application.gateway.httpclient.commons.client.postClient
import com.global.payment.application.gateway.httpclient.user.UserApiClient
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean

@ConfigurationProperties(prefix = "client.user-api")
data class UserApiConfiguration(
    override val baseUrl: String,
    override val responseTimeout: Long,
    override val connectionTimeout: Int,
) : ClientConfiguration<UserApiClient> {
    @Bean
    override fun createClient(): UserApiClient {
        return createWebClient().postClient()
    }
}


