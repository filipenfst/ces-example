package com.global.payment.application.resource.subscription

import io.micronaut.serde.annotation.Serdeable

@Serdeable
data class SubscriptionStatusResponse(
    val isSubscribed: Boolean
)
