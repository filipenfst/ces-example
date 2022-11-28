package com.global.payment.application.commons

import wiremock.com.jayway.jsonpath.DocumentContext
import wiremock.com.jayway.jsonpath.JsonPath

fun String.payload() = {}.javaClass.getResource("/__files/$this")!!.readText()

inline fun String.payload(builder: DocumentContext.() -> Unit): String = JsonPath.parse(payload()).also {
    it.builder()
}.jsonString()
