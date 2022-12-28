package com.global.payment.application.resource.subscription

import com.global.payment.commons.logger.logInfo
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Header
import io.micronaut.http.annotation.PathVariable
import java.util.UUID

@Controller("/user/{userId}")
open class UserController {
    @Get
    open suspend fun findUser(
        @PathVariable userId: UUID,
        @Header("X-B3-TraceId") traceId:String
    ): UserResponse = UserResponse(
        id = userId,
        name = "Name-$userId"
    ).also {
        logInfo("Returning user:$it with traceId = $traceId")
    }
}
