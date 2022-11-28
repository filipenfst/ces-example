package com.global.payment.domain.user.exception

class MissingUserException(
    override val message: String,
    override val cause: Throwable? = null
) : Exception(message, cause)
