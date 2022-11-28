package com.global.payment.domain.user.services

import com.global.payment.domain.user.entities.User
import java.util.UUID

interface UserFinderPort {
    suspend fun findUser(id: UUID): User?
}
