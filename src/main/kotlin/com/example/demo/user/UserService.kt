package com.example.demo.user

import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture

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
