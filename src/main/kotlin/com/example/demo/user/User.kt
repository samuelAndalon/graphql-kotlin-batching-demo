package com.example.demo.user


data class User(val id: Int, val name: String, val lastName: String)

fun User.toGraphQLUser(): GraphQLUser = GraphQLUser("$name $lastName")

data class GraphQLUser(val name: String)
