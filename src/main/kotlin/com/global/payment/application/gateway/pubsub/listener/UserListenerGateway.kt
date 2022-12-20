package com.global.payment.application.gateway.pubsub.listener

import com.global.payment.commons.logger.Logger
import com.global.payment.commons.logger.logInfo
import io.micronaut.gcp.pubsub.annotation.PubSubListener
import io.micronaut.gcp.pubsub.annotation.Subscription

@PubSubListener
class UserListenerGateway(
    private val logger: Logger
) {

    @Subscription("\${pubsub.user.topic}")
    fun onMessage(data: String){
        logger.logInfo("UserAccess RECEIVED | $data")
    }

}