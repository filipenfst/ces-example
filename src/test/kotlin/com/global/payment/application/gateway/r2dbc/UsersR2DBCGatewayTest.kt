package com.global.payment.application.gateway.r2dbc

import com.global.payment.application.IntegrationTests
import com.global.payment.application.commons.assertThat
import com.global.payment.application.gateway.r2dbc.entities.AppEntity
import com.global.payment.application.gateway.r2dbc.entities.MerchantEntity
import com.global.payment.application.gateway.r2dbc.entities.toEntity
import com.global.payment.application.gateway.r2dbc.integration.AppRepository
import com.global.payment.application.gateway.r2dbc.integration.MerchantRepository
import com.global.payment.application.gateway.r2dbc.integration.UserRepository
import com.global.payment.domain.user.entities.User
import jakarta.inject.Inject
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.asFlux
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.apache.commons.lang3.RandomStringUtils
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

internal class UsersR2DBCGatewayTest : IntegrationTests {
    @Inject
    private lateinit var userRepository: UserRepository

    @Inject
    private lateinit var merchantRepository: MerchantRepository

    @Inject
    private lateinit var classUnderTest: UsersR2DBCGateway

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
                printContext()
                userRepository.save(it.toEntity())
                printContext()
            }

            merchant.also {
                merchantRepository.save(it)
            }

            userRepository.addUserToMerchant(users[1].id, merchant.id)

            apps.forEach {
                appRepository.save(it)
            }

            merchantRepository.addAppToMerchant(appId = apps[1].id, merchantId = merchant.id)
        }
    }

    @Test
    fun `test list user for merchant successfully`(): Unit = runBlocking {
        printContext()
        transactionDecorator.withTransaction<String?> {
            println("----------1--")
            printContext()
            println("----------1--")
            classUnderTest.listUsersForMerchantByMid(merchant.mid)
                .asFlux()
                .contextWrite {
                    it
                }.asFlow()
                .assertThat {
                    hasSize(1).isEqualTo(listOf(users[1]))
                }
            null
        }
    }

    @Test
    fun `test check user has access to app`(): Unit = runBlocking {
        transactionDecorator.withTransaction {
            classUnderTest.hasAccessToApp(userId = users[1].id, applicationId = apps[1].applicationId)
        }.assertThat()
            .isEqualTo(true)
    }

    @Test
    fun `test check user does not have access to app`(): Unit = runBlocking {
        transactionDecorator.withTransaction {
            classUnderTest.hasAccessToApp(userId = users[1].id, applicationId = apps[0].applicationId)
        }.assertThat()
            .isEqualTo(false)
    }

}
