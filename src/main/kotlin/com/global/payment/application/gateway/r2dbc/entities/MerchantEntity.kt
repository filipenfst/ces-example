package com.global.payment.application.gateway.r2dbc.entities

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.*

const val MERCHANT_TABLE_NAME = "merchant"
const val MERCHANT_USERS_TABLE_NAME = "merchant_users"

@Table(MERCHANT_TABLE_NAME)
data class MerchantEntity(
    @field:Id
    val id: Int? = null,
    val externalId: UUID,
    val mid: String,
    val name: String
)
