package com.example.demo.product


data class Product(
    val id: Int,
    val summary: ProductSummary? = null,
    val details: ProductDetails? = null
)

data class ProductSummary(val name: String)
data class ProductDetails(val rating: String)
