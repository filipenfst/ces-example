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
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.ReactorContext
import kotlinx.coroutines.runBlocking
import org.apache.commons.lang3.RandomStringUtils
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import reactor.util.context.Context
import java.util.UUID

internal class UsersR2DBCGatewayTest : IntegrationTests {
    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var merchantRepository: MerchantRepository

    @Autowired
    private lateinit var classUnderTest: UsersR2DBCGateway

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
    fun setup(): Unit = runBlocking(ReactorContext(Context.empty())) {
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
    fun `test list user for merchant successfully`(): Unit = runBlocking {
        transactionDecorator.withTransaction {
            classUnderTest.listUsersForMerchantByMid(merchant.mid)
                .assertThat {
                    hasSize(1).isEqualTo(listOf(users[1]))
                }
        }
    }

    @Test
    fun `test check user has access to app`(): Unit = runBlocking {
        transactionDecorator.withTransaction {
            classUnderTest.hasAccessToApp(userId = users[1].id, applicationId = apps[1].applicationId)
                .assertThat()
                .isEqualTo(true)
        }
    }

    @Test
    fun `test check user does not have access to app`(): Unit = runBlocking {
        transactionDecorator.withTransaction {
            classUnderTest.hasAccessToApp(userId = users[1].id, applicationId = apps[0].applicationId)
                .assertThat()
                .isEqualTo(false)
        }
    }

}
