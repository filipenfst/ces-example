package com.global.payment.application.gateway.pubsub.publisher

import java.util.UUID

data class UserAccessDTO(
    private val userID: UUID,
    private val appID: String,
    private val hasAccess: Boolean
) {

}