package com.global.payment.application.gateway.r2dbc.entities

import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import java.util.*


const val APP_TABLE_NAME = "app"
const val MERCHANT_SUBSCRIPTION_TABLE_NAME = "merchants_subscriptions"

@MappedEntity(APP_TABLE_NAME)
data class AppEntity(
    @field:Id
    val id: UUID,
    val applicationId: String,
    val name: String
)
