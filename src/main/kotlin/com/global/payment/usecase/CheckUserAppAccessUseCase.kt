package com.global.payment.usecase

import com.global.payment.domain.user.exception.MissingUserException
import com.global.payment.domain.user.services.UserAppAccessPort
import com.global.payment.domain.user.services.UserFinderPort
import java.util.UUID

class CheckUserAppAccessUseCase(
    private val userAppAccessPort: UserAppAccessPort,
    private val userFinderPort: UserFinderPort,
) {

    suspend fun execute(userID: UUID, appID: String): Boolean {
        if (userFinderPort.findUser(userID) == null) {
            throw MissingUserException(message = "No user with id $userID was found")
        }
        return userAppAccessPort.hasAccessToApp(userID, appID)
    }
}
