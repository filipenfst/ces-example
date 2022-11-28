package com.global.payment.domain.user.services

import java.util.UUID

interface UserAppAccessPort {
    suspend fun hasAccessToApp(userId: UUID, applicationId: String): Boolean
}
