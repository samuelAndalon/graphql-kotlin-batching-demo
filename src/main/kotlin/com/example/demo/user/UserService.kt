package com.example.demo.user

import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.stereotype.Service
import reactor.kotlin.core.publisher.toMono
import java.time.Duration
import java.util.concurrent.CompletableFuture

@Service
class UserService {
    private val users = mapOf(
        1 to User(1, "John", "Doe"),
        2 to User(2, "Jane", "Doe")
    )

    suspend fun getUser(id: Int): User? =
        users[id]?.toMono()?.delayElement(Duration.ofSeconds(1))?.awaitSingle()

    fun getUsers(ids: Set<Int>): CompletableFuture<Map<Int, User?>> =
        CompletableFuture.completedFuture(
            ids.associateWith { id -> users[id] }
        )
}
