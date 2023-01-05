package com.global.payment.application.gateway

import com.github.tomakehurst.wiremock.client.WireMock
import com.global.payment.application.IntegrationTests
import com.global.payment.application.commons.assertThat
import com.global.payment.application.commons.wiremock.WireMockInitializer
import com.global.payment.application.commons.wiremock.withJsonResponseFile
import com.global.payment.application.gateway.r2dbc.TransactionDecorator
import com.global.payment.application.gateway.r2dbc.entities.AppEntity
import com.global.payment.application.gateway.r2dbc.entities.MerchantEntity
import com.global.payment.application.gateway.r2dbc.entities.UserEntity
import com.global.payment.application.gateway.r2dbc.entities.toEntity
import com.global.payment.application.gateway.r2dbc.integration.AppRepository
import com.global.payment.application.gateway.r2dbc.integration.MerchantRepository
import com.global.payment.application.gateway.r2dbc.integration.UserRepository
import com.global.payment.domain.user.entities.User
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.runBlocking
import org.apache.commons.lang3.RandomStringUtils
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID

internal class UserGatewayProxyTest : IntegrationTests {
    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var merchantRepository: MerchantRepository

    @Autowired
    private lateinit var classUnderTest: UserGatewayProxy

    @Autowired
    private lateinit var appRepository: AppRepository

    @Autowired
    private lateinit var transactionDecorator: TransactionDecorator


    private val users = (1..2).map {
        User(
            id = UUID.randomUUID(),
            name = RandomStringUtils.randomAlphanumeric(10)
        )
    }
    private val apps = (1..2).map {
        AppEntity(
            externalId = UUID.randomUUID(),
            applicationId = RandomStringUtils.randomAlphanumeric(10),
            name = RandomStringUtils.randomAlphanumeric(10)
        )
    }

    private val merchant = MerchantEntity(
        externalId = UUID.randomUUID(),
        mid = RandomStringUtils.randomAlphanumeric(10),
        name = RandomStringUtils.randomAlphanumeric(10)
    )

    @BeforeEach
    fun setup(): Unit = runBlocking {
        transactionDecorator.withTransaction {
            val usersDB = users.map {
                userRepository.save(it.toEntity()).awaitSingle()
            }
            val merchantDb = merchant.let {
                merchantRepository.save(it).awaitSingle()
            }

            userRepository.addUserToMerchant(usersDB[1].id!!, merchantDb.id!!)


            val appsDb = apps.map {
                appRepository.save(it).awaitSingle()
            }

            merchantRepository.addAppToMerchant(appId = appsDb[1].id!!, merchantId = merchantDb.id!!)
        }
    }


    @Test
    fun `when user does not exist locally it should get from httpclient`(): Unit = runBlocking {
        transactionDecorator.withTransaction {
            val id = UUID.randomUUID()

            WireMockInitializer.wireMock.stubFor(
                WireMock.get(WireMock.urlEqualTo("/user/$id"))
                    .willReturn(
                        WireMock.aResponse()
                            .withStatus(200)
                            .withJsonResponseFile("userResponse.json") {
                                set("$.id", id.toString())
                            }
                    )
            )

            classUnderTest.findUser(id)
                .assertThat().ignoringFields("id").isEqualTo(
                    UserEntity(
                        externalId = id,
                        name = "Name test"
                    )
                )
        }
    }

    @Test
    fun `when user does not exist locally it should return null when http client returns not found`(): Unit =
        runBlocking {
            transactionDecorator.withTransaction {
                val id = UUID.randomUUID()

                WireMockInitializer.wireMock.stubFor(
                    WireMock.get(WireMock.urlEqualTo("/user/$id"))
                        .willReturn(
                            WireMock.aResponse()
                                .withStatus(404)
                                .withJsonResponseFile("userResponse.json") {
                                    set("$.id", id.toString())
                                }
                        )
                )

                classUnderTest.findUser(id)
                    .assertThat().isNull()
            }
        }
}
