package com.global.payment.application.resource.subscription

import com.global.payment.commons.logger.logInfo
import com.global.payment.usecase.CheckUserAppAccessUseCase
import jakarta.validation.constraints.NotBlank
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/user/{userId}/subscription")
class SubscriptionController(
    private val checkUserAppAccessUseCase: CheckUserAppAccessUseCase,
) {
    @GetMapping
    suspend fun checkSubscriptionStatus(
        @PathVariable userId: UUID,
        @RequestParam @NotBlank appId: String
    ): SubscriptionStatusResponse {
        logInfo(msg = "Checking subscription status for user: $userId and app $appId")
        return SubscriptionStatusResponse(
            isSubscribed = checkUserAppAccessUseCase.execute(userId, appId)
        ).also {
            logInfo(msg = "Returning: $it")
        }
    }
}
