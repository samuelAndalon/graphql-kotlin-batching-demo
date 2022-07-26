package com.example.demo

import com.expediagroup.graphql.generator.execution.KotlinDataFetcherFactoryProvider
import com.expediagroup.graphql.generator.execution.SimpleKotlinDataFetcherFactoryProvider
import com.expediagroup.graphql.generator.hooks.SchemaGeneratorHooks
import com.expediagroup.graphql.server.spring.execution.SpringDataFetcher
import graphql.schema.DataFetcherFactory
import graphql.schema.DataFetchingEnvironment
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import kotlin.reflect.KFunction
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.createType

@SpringBootApplication
class DemoApplication {
    @Bean
    fun schemaGeneratorHooks(): SchemaGeneratorHooks =
        object : SchemaGeneratorHooks {
            override fun willResolveMonad(type: KType): KType =
                when (type.classifier) {
                    Mono::class -> type.arguments.firstOrNull()?.type
                    Flux::class -> type.arguments.firstOrNull()?.let { argument ->
                        List::class.createType(
                            arguments = listOf(KTypeProjection(argument.variance, argument.type))
                        )
                    }
                    else -> type
                } ?: type
        }

    @Bean
    fun dataFetcherFactoryProvider(
        applicationContext: ApplicationContext
    ): KotlinDataFetcherFactoryProvider =
        object : SimpleKotlinDataFetcherFactoryProvider() {
            override fun functionDataFetcherFactory(target: Any?, kFunction: KFunction<*>): DataFetcherFactory<Any?> =
                DataFetcherFactory {
                    object : SpringDataFetcher(target, kFunction, applicationContext) {
                        override fun get(environment: DataFetchingEnvironment): Any? {
                            return when(val result = super.get(environment)) {
                                is Mono<*> -> result.toFuture()
                                is Flux<*> -> result.collectList().toFuture()
                                else -> result
                            }
                        }
                    }
                }
        }
}

fun main(args: Array<String>) {
	runApplication<DemoApplication>(*args)
}
