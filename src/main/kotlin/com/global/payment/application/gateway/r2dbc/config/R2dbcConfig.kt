package com.global.payment.application.gateway.r2dbc.config

import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories


@EnableR2dbcRepositories(basePackages = ["com.global.payment.application.gateway.r2dbc"])
@Configuration
class R2dbcConfig
