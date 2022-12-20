package com.global.payment.domain.user.services

import jakarta.inject.Singleton
import java.util.UUID


interface UserPublisherPort {

    fun send(userId: UUID, appID: String, hasAccess: Boolean)
}