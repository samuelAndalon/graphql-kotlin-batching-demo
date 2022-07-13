package com.example.demo

import com.expediagroup.graphql.dataloader.KotlinDataLoader
import org.dataloader.DataLoader
import org.dataloader.DataLoaderFactory
import org.dataloader.DataLoaderOptions
import org.dataloader.stats.SimpleStatisticsCollector
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DemoApplicationConfiguration {
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
}
