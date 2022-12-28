package com.global.payment.application.resource.subscription

import com.github.tomakehurst.wiremock.client.WireMock
import com.global.payment.application.IntegrationTests
import com.global.payment.application.commons.wiremock.WireMockInitializer
import com.global.payment.application.commons.wiremock.withJsonResponseFile
import com.global.payment.application.gateway.r2dbc.TransactionDecorator
import com.global.payment.application.gateway.r2dbc.entities.AppEntity
import com.global.payment.application.gateway.r2dbc.entities.MerchantEntity
import com.global.payment.application.gateway.r2dbc.entities.toEntity
import com.global.payment.application.gateway.r2dbc.integration.AppRepository
import com.global.payment.application.gateway.r2dbc.integration.MerchantRepository
import com.global.payment.application.gateway.r2dbc.integration.UserRepository
import com.global.payment.domain.user.entities.User
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import jakarta.inject.Inject
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.runBlocking
import org.apache.commons.lang3.RandomStringUtils
import org.hamcrest.CoreMatchers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

internal class SubscriptionControllerTest : IntegrationTests {
    @Inject
    private lateinit var spec: RequestSpecification

    @Inject
    private lateinit var userRepository: UserRepository

    @Inject
    private lateinit var merchantRepository: MerchantRepository

    @Inject
    private lateinit var appRepository: AppRepository

    @Inject
    private lateinit var transactionDecorator: TransactionDecorator


    private val users = (1..2).map {
        User(
            id = UUID.randomUUID(),
            name = RandomStringUtils.randomAlphanumeric(10)
        )
    }
    private val apps = (1..2).map {
        AppEntity(
            id = UUID.randomUUID(),
            applicationId = RandomStringUtils.randomAlphanumeric(10),
            name = RandomStringUtils.randomAlphanumeric(10)
        )
    }

    private val merchant = MerchantEntity(
        id = UUID.randomUUID(),
        mid = RandomStringUtils.randomAlphanumeric(10),
        name = RandomStringUtils.randomAlphanumeric(10)
    )

    @BeforeEach
    fun setup(): Unit = runBlocking {
        transactionDecorator.withTransaction {
            users.forEach {
                userRepository.save(it.toEntity()).awaitSingle()
            }
            merchant.also {
                merchantRepository.save(it).awaitSingle()
            }

            userRepository.addUserToMerchant(users[1].id, merchant.id).awaitSingle()


            apps.forEach {
                appRepository.save(it).awaitSingle()
            }

            merchantRepository.addAppToMerchant(appId = apps[1].id, merchantId = merchant.id).awaitSingle()
        }
    }

    @Test
    fun `When user has access to app it should return true`() {
        spec.given().request()
            .accept(ContentType.JSON)
            .contentType(ContentType.JSON)
            .queryParam("appId", apps[1].applicationId)
            .`when`().get("/user/${users[1].id}/subscription")
            .then()
            .statusCode(200)
            .body(
                "isSubscribed", CoreMatchers.`is`(true)
            )
    }

    @Test
    fun `When user does not has access to app it should return false`() {
        spec.given().request()
            .accept(ContentType.JSON)
            .contentType(ContentType.JSON)
            .queryParam("appId", apps[1].applicationId)
            .`when`().get("/user/${users[0].id}/subscription")
            .then()
            .statusCode(200)
            .body(
                "isSubscribed", CoreMatchers.`is`(false)
            )
    }

    @Test
    fun `When user exist and dont have access to app it should return false`() {
        val id = UUID.randomUUID().toString()
        WireMockInitializer.wireMock.stubFor(
            WireMock.get(WireMock.urlEqualTo("/user/$id"))
                .withHeader("X-B3-TraceId", WireMock.containing("269736db4c032be3"))
                .withHeader("X-B3-SpanId", WireMock.matching(".*"))
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(200)
                        .withJsonResponseFile("userResponse.json") {
                            set("$.id", id)
                        }
                )
        )
        spec.given().request()
            .accept(ContentType.JSON)
            .headers(
                "X-B3-TraceId", "269736db4c032be3",
                "X-B3-SpanId", "b5c553dca8e73c90",
                "X-B3-Sampled", "1",
            )
            .contentType(ContentType.JSON)
            .queryParam("appId", apps[1].applicationId)
            .`when`().get("/user/$id/subscription")
            .then()
            .statusCode(200)
            .body(
                "isSubscribed", CoreMatchers.`is`(false)
            )
    }

    @Test
    fun `When user does not  exists it should fail`() {
        spec.given().request()
            .accept(ContentType.JSON)
            .contentType(ContentType.JSON)
            .queryParam("appId", apps[1].applicationId)
            .`when`().get("/user/${UUID.randomUUID()}/subscription")
            .then()
            .statusCode(400)
    }
}
