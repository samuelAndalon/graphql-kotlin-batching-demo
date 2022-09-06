package com.example.demo

import com.example.demo.product.Product
import com.example.demo.product.ProductDataSource
import com.example.demo.product.ProductRequest
import com.example.demo.user.GraphQLUser
import com.example.demo.user.User
import com.example.demo.user.UserDataSource
import com.example.demo.user.UserService
import com.example.demo.user.toGraphQLUser
import com.expediagroup.graphql.server.operations.Query
import graphql.schema.DataFetchingEnvironment
import graphql.schema.SelectedField
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono
import java.time.Duration
import java.util.concurrent.CompletableFuture

@Component
class SimpleQuery(
    val userDataSource: UserDataSource,
    val productDataSource: ProductDataSource
) : Query {

    fun user(id: Int, environment: DataFetchingEnvironment): CompletableFuture<GraphQLUser?> =
        userDataSource.getUser(id, environment).thenApplyAsync {
            it?.toGraphQLUser()
        }

    fun product(id: Int, environment: DataFetchingEnvironment): CompletableFuture<Product?> =
        productDataSource.getProduct(
            ProductRequest(id, environment.selectionSet.immediateFields.map(SelectedField::getName)),
            environment
        )

    fun hello(): Mono<String> =
        "graphql kotlin".toMono().delayElement(Duration.ofSeconds(1))

    fun double(numbers: List<Int>): Flux<Int> =
        numbers.toFlux().flatMap { number ->
            (number * 2).toMono().delayElement(Duration.ofSeconds(1))
        }

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
