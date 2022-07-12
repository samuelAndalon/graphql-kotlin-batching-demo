package com.example.demo

import com.expediagroup.graphql.dataloader.KotlinDataLoader
import com.expediagroup.graphql.server.operations.Query
import graphql.schema.DataFetchingEnvironment
import org.dataloader.DataLoader
import org.dataloader.DataLoaderFactory
import org.dataloader.DataLoaderOptions
import org.dataloader.stats.SimpleStatisticsCollector
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
    fun getUser(id: Int): CompletableFuture<User?> =
        userRepository.getUser(id)

    fun getUsers(ids: List<Int>): CompletableFuture<List<User?>> {
        val users = ids.map { id -> userRepository.getUser(id).get() }
        return CompletableFuture.completedFuture(users)
    }
}

class UserDataSource(
    private val userService: UserService = UserService()
) : KotlinDataLoader<Int, User?> {
    override val dataLoaderName: String = "UserDataLoader"

    override fun getDataLoader(): DataLoader<Int, User?> =
        DataLoaderFactory.newDataLoader(
            { ids -> userService.getUsers(ids) },
            DataLoaderOptions.newOptions().setStatisticsCollector(::SimpleStatisticsCollector)
        )

    fun getUser(id: Int, environment: DataFetchingEnvironment): CompletableFuture<User?> =
        environment
            .getDataLoader<Int, User?>("UserDataLoader")
            .load(id)
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
    val userDataSource = UserDataSource()

    fun user(id: Int, environment: DataFetchingEnvironment): CompletableFuture<User?> =
        userDataSource.getUser(id, environment)
}
