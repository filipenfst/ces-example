package com.global.payment.application.gateway.r2dbc.entities

import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import java.util.*

const val MERCHANT_TABLE_NAME = "merchant"
const val MERCHANT_USERS_TABLE_NAME = "merchant_users"

@MappedEntity(MERCHANT_TABLE_NAME)
data class MerchantEntity(
    @field:Id
    val id: UUID,
    val mid: String,
    val name: String
)
