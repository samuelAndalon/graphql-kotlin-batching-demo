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

data class ParentQueryModel(val childQueryModel: ChildQueryModel? = ChildQueryModel("defaultValue"))
data class ChildQueryModel(val value: String? = "defaultValue")
data class PriceRange(val min: Double, val max: Double)

@Component
class SimpleQuery(
    val userDataSource: UserDataSource,
    val userService: UserService,
    val productDataSource: ProductDataSource
) : Query {

    suspend fun returnTrueIfChildValueDefaults(parentQueryModel: ParentQueryModel): Boolean {
        return parentQueryModel.childQueryModel?.value == "defaultValue"
    }

    fun priceRange(range: PriceRange, environment: DataFetchingEnvironment): Double =
        range.min

    /*suspend fun user(id: Int): User? =
        userService.getUser(id)*/

    fun user(id: Int, environment: DataFetchingEnvironment): CompletableFuture<GraphQLUser?> =
        environment
            .getDataLoader<Int, User?>("UserDataLoader")
            .load(id)
            .thenApplyAsync {
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
