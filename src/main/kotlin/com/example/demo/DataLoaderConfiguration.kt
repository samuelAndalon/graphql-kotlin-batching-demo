package com.example.demo

import com.example.demo.extensions.getLoadContext
import com.example.demo.product.Product
import com.example.demo.product.ProductRequest
import com.example.demo.product.ProductService
import com.example.demo.user.User
import com.example.demo.user.UserService
import com.expediagroup.graphql.dataloader.KotlinDataLoader
import com.expediagroup.graphql.generator.extensions.get
import com.expediagroup.graphql.generator.extensions.getOrDefault
import graphql.com.google.common.base.Optional
import graphql.schema.DataFetchingEnvironment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.future.asCompletableFuture
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.dataloader.BatchLoaderEnvironment
import org.dataloader.DataLoader
import org.dataloader.DataLoaderFactory
import org.dataloader.DataLoaderOptions
import org.dataloader.stats.SimpleStatisticsCollector
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import kotlin.coroutines.EmptyCoroutineContext

@Configuration
class DataLoaderConfiguration {
    @Bean
    fun userDataLoader(
        userService: UserService
    ): KotlinDataLoader<Int, User?> =
        object : KotlinDataLoader<Int, User?> {
            override val dataLoaderName: String = "UserDataLoader"
            override fun getDataLoader(): DataLoader<Int, User?> =
                DataLoaderFactory.newMappedDataLoader(
                    { ids ->
                        userService.getUsers(ids)
                    },
                    DataLoaderOptions.newOptions().setStatisticsCollector(::SimpleStatisticsCollector)
                )
        }

    @Bean
    fun productDataLoader(
        productService: ProductService
    ): KotlinDataLoader<ProductRequest, Product?> =
        object : KotlinDataLoader<ProductRequest, Product?> {
            override val dataLoaderName: String = "ProductDataLoader"
            override fun getDataLoader(): DataLoader<ProductRequest, Product?> =
                DataLoaderFactory.newDataLoader(
                    { requests, environment ->
                        val coroutineScope =
                            environment.getLoadContext<DataFetchingEnvironment>()?.graphQlContext?.get<CoroutineScope>()
                            ?: CoroutineScope(EmptyCoroutineContext)

                        coroutineScope.async {
                            productService.getProducts(requests)
                        }.asCompletableFuture()
                    },
                    DataLoaderOptions.newOptions().setStatisticsCollector(::SimpleStatisticsCollector)
                )
        }
}
