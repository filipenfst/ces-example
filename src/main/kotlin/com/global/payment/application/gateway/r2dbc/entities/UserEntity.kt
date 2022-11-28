package com.global.payment.application.gateway.r2dbc.entities

import com.global.payment.domain.user.entities.User
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactive.asFlow
import org.reactivestreams.Publisher
import java.util.*

const val USER_TABLE_NAME = "users"

@MappedEntity(USER_TABLE_NAME)
data class UserEntity(
    @field:Id
    val id: UUID,
    val name: String
) {
    fun toDomain() = User(
        id = id,
        name = name,
    )
}

fun Flow<UserEntity>.toDomain() = map { it.toDomain() }
fun Publisher<UserEntity>.toDomain() = asFlow().toDomain()

fun User.toEntity() = UserEntity(
    id = id,
    name = name
)
