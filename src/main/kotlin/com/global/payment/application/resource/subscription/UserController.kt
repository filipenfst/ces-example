package com.global.payment.application.resource.subscription

import com.global.payment.commons.logger.logInfo
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/user/{userId}")
class UserController {
    @GetMapping
    suspend fun findUser(
        @PathVariable userId: UUID,
        @RequestHeader("X-B3-TraceId") traceId: String
    ): UserResponse = UserResponse(
        id = userId,
        name = "Name-$userId"
    ).also {
        logInfo("Returning user:$it with traceId = $traceId")
    }
}
