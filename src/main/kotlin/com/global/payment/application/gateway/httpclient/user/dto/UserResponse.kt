package com.global.payment.application.gateway.httpclient.user.dto

import com.global.payment.domain.user.entities.User
import io.micronaut.serde.annotation.Serdeable
import java.util.UUID

@Serdeable
data class UserResponse(
    private val id: UUID,
    private val name: String
) {
    fun toDomain() = User(id = id, name = name)
}
