package com.global.payment.application.resource.subscription

import com.global.payment.application.gateway.r2dbc.TransactionDecorator
import com.global.payment.commons.logger.logInfo
import com.global.payment.usecase.CheckUserAppAccessUseCase
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import java.util.UUID
import javax.validation.constraints.NotBlank

@Controller("/user/{userId}/subscription")
open class SubscriptionController(
    private val checkUserAppAccessUseCase: CheckUserAppAccessUseCase,
    private val transactionDecorator: TransactionDecorator,
) {
    @Get
    open suspend fun checkSubscriptionStatus(
        @PathVariable userId: UUID,
        @QueryValue @NotBlank appId: String
    ): SubscriptionStatusResponse = transactionDecorator.withTransaction {
        logInfo(msg = "Checking subscription status for user: $userId and app $appId")
        SubscriptionStatusResponse(
            isSubscribed = checkUserAppAccessUseCase.execute(userId, appId)
        ).also {
            logInfo(msg = "Returning: $it")
        }
    }
}
