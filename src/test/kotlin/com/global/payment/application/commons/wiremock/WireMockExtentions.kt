package com.global.payment.application.commons.wiremock

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.global.payment.application.commons.payload
import wiremock.com.jayway.jsonpath.DocumentContext

internal fun ResponseDefinitionBuilder.withJsonResponseBody(
    jsonResponseBody: (() -> String)
): ResponseDefinitionBuilder = withHeader("Content-Type", "application/json")
    .withBody(jsonResponseBody())

internal fun ResponseDefinitionBuilder.withJsonResponseFile(
    fileName: String,
    builder: DocumentContext.() -> Unit
): ResponseDefinitionBuilder = withJsonResponseBody {
    fileName.payload(builder)
}
