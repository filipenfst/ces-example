package com.global.payment.application.gateway.httpclient.user

import com.global.payment.application.IntegrationTests
import com.global.payment.application.commons.assertThat
import com.global.payment.commons.logger.logError
import com.global.payment.domain.user.entities.User
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.util.UUID

internal class UserHttpClientGatewayTest : IntegrationTests {

    private lateinit var userWebClientGateway: UserHttpClientGateway

    @Test
    fun testReturnUserSuccessfully(): Unit = runBlocking {
//        WireMockInitializer.wireMock.stubFor(
//            get(urlEqualTo("/user/09a93f31-1344-45e7-b7cf-ba7429ca3f83"))
//                .willReturn(
//                    aResponse()
//                        .withStatus(200)
//                        .withJsonResponseFile("userResponse.json") { }
//                )
//        )

        val id = UUID.fromString("09a93f31-1344-45e7-b7cf-ba7429ca3f83")
        userWebClientGateway.findUser(id).assertThat()
            .isEqualTo(User(id, "Name test"))

//        WireMockInitializer.wireMock.verify(
//            1,
//            getRequestedFor(urlMatching("/user/.*"))
//        )
    }

    @Test
    fun testCircuitBreaker(): Unit = runBlocking {
//        WireMockInitializer.wireMock.stubFor(
//            get(urlEqualTo("/user/09a93f31-1344-45e7-b7cf-ba7429ca3f83"))
//                .willReturn(
//                    aResponse()
//                        .withStatus(401)
//                        .withJsonResponseFile("userResponse.json") { }
//                )
//        )

        val id = UUID.fromString("09a93f31-1344-45e7-b7cf-ba7429ca3f83")
        repeat(3) {
            kotlin.runCatching {
                userWebClientGateway.findUser(id)
            }.onFailure {
                logError("test error", ex = it)
            }
        }

//        WireMockInitializer.wireMock.verify(
//            2,
//            getRequestedFor(urlMatching("/user/.*"))
//        )
    }
}
