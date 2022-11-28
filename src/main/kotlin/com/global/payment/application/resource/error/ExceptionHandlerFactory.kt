package com.global.payment.application.resource.error

import com.global.payment.commons.logger.logWarn
import com.global.payment.domain.user.exception.MissingUserException
import io.micronaut.context.annotation.Factory
import io.micronaut.http.HttpResponse
import io.micronaut.http.server.exceptions.ExceptionHandler
import jakarta.inject.Singleton

@Factory
class ExceptionHandlerFactory {
    @Singleton
    fun missingUserExceptionHandler() =
        ExceptionHandler<MissingUserException, HttpResponse<ErrorResponse>> { request, exception ->
            logWarn("Error on request $request", ex = exception)
            HttpResponse.badRequest(ErrorResponse(exception.message))
        }
}
