package com.global.payment.application.resource.subscription

import io.micronaut.serde.annotation.Serdeable
import java.util.UUID

@Serdeable
data class UserResponse(
    val id: UUID,
    val name: String
)
