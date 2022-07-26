package com.example.demo.product

import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.stereotype.Service
import reactor.kotlin.core.publisher.toMono
import java.time.Duration

@Service
class ProductService {
    val products = mapOf(
        1 to Product(1),
        2 to Product(2)
    )

    val productsSummary = mapOf(
        1 to ProductSummary("Product 1"),
        2 to ProductSummary("Product 2")
    )

    val productsDetails = mapOf(
        1 to ProductDetails("5 out of 10"),
        2 to ProductDetails("10 out of 10")
    )

    /**
     * let's assume data batch loader provides 4 request to getProducts
     * - 2 for productId 1 fetching summary and details respectively
     * - 2 for productId 2 fetching summary and details respectively
     *
     *  here we would need to aggregate 2 requests for each productId into 1 like this
     *  - 1 request for productId 1 fetching summary and details
     *  - 1 request for productId 1 fetching summary and details
     */
    suspend fun getProducts(requests: List<ProductRequest>): List<Product?> {
        val reducedRequest =
            requests.groupBy(ProductRequest::id)
                .mapValues { (productId, requests) ->
                    ProductRequest(
                        productId,
                        requests.map(ProductRequest::fields).flatten().distinct()
                    )
                }.values.toList()

        val results =
            reducedRequest.mapNotNull { productRequest ->
                products[productRequest.id]?.let { product ->
                    Product(
                        product.id,
                        productsSummary[product.id].takeIf { productRequest.fields.contains("summary") },
                        productsDetails[product.id].takeIf { productRequest.fields.contains("details") }
                    )
                }
            }.associateBy(Product::id)

        return requests
            .map { request ->
                results[request.id]
            }.toMono().delayElement(Duration.ofSeconds(2)).awaitSingle()
    }
}
