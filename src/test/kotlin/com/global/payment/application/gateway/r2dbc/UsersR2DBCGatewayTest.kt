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
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.runBlocking
import org.apache.commons.lang3.RandomStringUtils
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

internal class UsersR2DBCGatewayTest : IntegrationTests {
    @Inject
    private lateinit var userRepository: UserRepository

    @Inject
    private lateinit var merchantRepository: MerchantRepository

    @Inject
    private lateinit var classUnderTest: UsersR2DBCGateway

    @Inject
    private lateinit var appRepository: AppRepository


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

    @Test
    fun `test list user for merchant successfully`(): Unit = runBlocking {
        classUnderTest.listUsersForMerchantByMid(merchant.mid).assertThat {
            hasSize(1).isEqualTo(listOf(users[1]))
        }
    }

    @Test
    fun `test check user has access to app`(): Unit = runBlocking {
        classUnderTest.hasAccessToApp(userId = users[1].id, applicationId = apps[1].applicationId)
            .assertThat()
            .isEqualTo(true)
    }

    @Test
    fun `test check user does not have access to app`(): Unit = runBlocking {
        classUnderTest.hasAccessToApp(userId = users[1].id, applicationId = apps[0].applicationId)
            .assertThat()
            .isEqualTo(false)
    }

}
