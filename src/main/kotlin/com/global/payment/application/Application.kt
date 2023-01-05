package com.global.payment.application

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import java.util.TimeZone


@OpenAPIDefinition(
    info = Info(
        title = "CES Micronaut Example",
        version = "0.1",
        description = "",
    )
)
@SpringBootApplication(scanBasePackages = ["com.global.payment.application"])
@ConfigurationPropertiesScan
class Application

fun main(vararg args: String) {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    runApplication<Application>(*args)
}
