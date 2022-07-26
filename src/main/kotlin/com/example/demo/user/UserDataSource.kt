package com.example.demo.user

import graphql.schema.DataFetchingEnvironment
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture

@Service
class UserDataSource {
    fun getUser(id: Int, environment: DataFetchingEnvironment): CompletableFuture<User?> =
        environment
            .getDataLoader<Int, User?>("UserDataLoader")
            .load(id)
}
