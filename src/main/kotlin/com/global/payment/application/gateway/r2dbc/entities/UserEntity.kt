package com.global.payment.application.gateway.r2dbc.entities

import com.global.payment.domain.user.entities.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactive.asFlow
import org.reactivestreams.Publisher
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.*

const val USER_TABLE_NAME = "users"

@Table(USER_TABLE_NAME)
data class UserEntity(
    @field:Id
    val id: Int?=null,
    val externalId: UUID,
    val name: String
) {
    fun toDomain() = User(
        id = externalId,
        name = name,
    )
}

fun Flow<UserEntity>.toDomain() = map { it.toDomain() }
fun Publisher<UserEntity>.toDomain() = asFlow().toDomain()

fun User.toEntity() = UserEntity(
    externalId = id,
    name = name
)
