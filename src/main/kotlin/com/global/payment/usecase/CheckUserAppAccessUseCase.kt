package com.global.payment.usecase

import com.global.payment.domain.user.exception.MissingUserException
import com.global.payment.domain.user.services.UserAppAccessPort
import com.global.payment.domain.user.services.UserFinderPort
import com.global.payment.domain.user.services.UserPublisherPort
import java.util.UUID

class CheckUserAppAccessUseCase(
    private val userAppAccessPort: UserAppAccessPort,
    private val userFinderPort: UserFinderPort,
    private val userPublisherPort: UserPublisherPort,
) {

    suspend fun execute(userID: UUID, appID: String): Boolean {
        if (userFinderPort.findUser(userID) == null) {
            throw MissingUserException(message = "No user with id $userID was found")
        }
        val hasAccess = userAppAccessPort.hasAccessToApp(userID, appID)
        userPublisherPort.send(userID, appID, hasAccess)
        return hasAccess
    }
}
