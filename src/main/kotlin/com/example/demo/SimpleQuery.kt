package com.example.demo

import com.expediagroup.graphql.server.operations.Query
import graphql.schema.DataFetchingEnvironment
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture

data class User(val id: Int, val name: String, val lastName: String)

@Service
class UserService {
    private val users = mapOf(
        1 to User(1, "John", "Doe"),
        2 to User(2, "Jane", "Doe")
    )

    fun getUsers(ids: Set<Int>): CompletableFuture<Map<Int, User?>> =
        CompletableFuture.completedFuture(
            ids.associateWith { id -> users[id] }
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
 * type User {
 *     id: Int!
 *     name: String!
 *     lastName: String!
 * }
 */
@Component
class SimpleQuery(val userDataSource: UserDataSource) : Query {
    fun user(id: Int, environment: DataFetchingEnvironment): CompletableFuture<User?> =
        userDataSource.getUser(id, environment)
}
