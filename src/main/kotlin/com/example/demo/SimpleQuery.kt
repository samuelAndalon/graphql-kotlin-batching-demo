package com.example.demo

import com.expediagroup.graphql.server.operations.Query
import kotlinx.coroutines.future.await
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture

data class User(
    val id: Int,
    val name: String,
    val lastName: String
)

class UserRepository {
    private val users = listOf(
        User(1, "John", "Doe"),
        User(2, "Jane", "Doe")
    )

    fun getUser(id: Int): CompletableFuture<User?> =
        CompletableFuture.completedFuture(
            users.firstOrNull { user -> user.id == id }
        )
}

class UserService(
    private val userRepository: UserRepository = UserRepository()
) {
    suspend fun getUser(id: Int): User? =
        userRepository.getUser(id).await()
}

/**
 * Schema Generator will generate:
 * type Query {
 *     user(id: Int!): User
 * }
 *
 * type User {
 *     id: Int!
 *     lastName: String!
 *     name: String!
 * }
 */
@Component
class SimpleQuery : Query {
    val userService = UserService()

    suspend fun user(id: Int): User? =
        userService.getUser(id)
}
