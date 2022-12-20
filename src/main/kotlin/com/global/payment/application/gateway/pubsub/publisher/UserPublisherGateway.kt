package com.global.payment.application.gateway.pubsub.publisher

import com.global.payment.commons.logger.Logger
import com.global.payment.commons.logger.logError
import com.global.payment.commons.logger.logInfo
import com.global.payment.domain.user.services.UserPublisherPort
import io.micronaut.gcp.pubsub.annotation.PubSubClient
import io.micronaut.gcp.pubsub.annotation.Topic
import jakarta.inject.Singleton
import java.util.*

@Singleton
class UserPublisherGateway(
    private val publisherClient: PublisherClient,
    private val logger: Logger
): UserPublisherPort {
    override fun send(userId: UUID, appID: String, hasAccess: Boolean) {
        val userAccess = UserAccessDTO(userID = userId, appID = appID, hasAccess = hasAccess)
        try {
            publisherClient.send(userAccess.toString())
            logger.logInfo("MESSAGE SENT TO THE TOPIC | ${userAccess.toString()}")
        } catch (ex : Exception){
            ex.message?.let { logger.logError(it) }
        }

    }


}

@PubSubClient
interface PublisherClient {

    @Topic("\${pubsub.user.topic}")
    fun send(data: String)

}