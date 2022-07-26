package com.example.demo.product

import graphql.schema.DataFetchingEnvironment
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture

data class ProductRequest(val id: Int, val fields: List<String> = listOf("summary", "details"))

@Service
class ProductDataSource {
    fun getProduct(
        request: ProductRequest,
        environment: DataFetchingEnvironment
    ): CompletableFuture<Product?> =
        environment
            .getDataLoader<ProductRequest, Product?>("ProductDataLoader")
            .load(request, environment)
}
