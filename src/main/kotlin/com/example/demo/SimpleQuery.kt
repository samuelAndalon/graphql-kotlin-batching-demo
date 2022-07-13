package com.example.demo

import com.expediagroup.graphql.dataloader.KotlinDataLoader
import com.expediagroup.graphql.server.operations.Query
import graphql.schema.DataFetchingEnvironment
import org.dataloader.DataLoader
import org.dataloader.DataLoaderFactory
import org.dataloader.DataLoaderOptions
import org.dataloader.stats.SimpleStatisticsCollector
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono
import java.util.concurrent.CompletableFuture

data class User(val id: Int, val name: String, val lastName: String)

@Repository
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

@Service
class UserService(private val userRepository: UserRepository) {
    fun getUsers(ids: Set<Int>): CompletableFuture<Map<Int, User?>> =
        CompletableFuture.completedFuture(
            ids.associateWith { id -> userRepository.getUser(id).get() }
        )
}

@Service
class UserDataLoader(private val userService: UserService) : KotlinDataLoader<Int, User?> {
    override val dataLoaderName: String = "UserDataLoader"
    override fun getDataLoader(): DataLoader<Int, User?> =
        DataLoaderFactory.newMappedDataLoader(
            { ids ->
                userService.getUsers(ids)
            },
            DataLoaderOptions.newOptions().setStatisticsCollector(::SimpleStatisticsCollector)
        )
}

@Service
class UserDataSource {
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
class SimpleQuery(
    val userDataSource: UserDataSource
) : Query {
    fun user(id: Int, environment: DataFetchingEnvironment): CompletableFuture<User?> =
        userDataSource.getUser(id, environment)

    fun foo(): Mono<List<String>> =
        listOf("one".toMono(), "two".toMono()) // List<Mono<String>>
            .toFlux()
            .flatMap { it }
            .collectList()

}
