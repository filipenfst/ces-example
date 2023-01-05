package com.global.payment.application.gateway.r2dbc.entities

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.*


const val APP_TABLE_NAME = "app"
const val MERCHANT_SUBSCRIPTION_TABLE_NAME = "merchants_subscriptions"

@Table(APP_TABLE_NAME)
data class AppEntity(
    @field:Id
    val id: Int? = null,
    val externalId: UUID,
    val applicationId: String,
    val name: String
)
