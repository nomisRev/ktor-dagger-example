package com.example

import com.example.features.comments.api.commentRoutes
import com.example.posts.postRoutes
import com.example.users.userRoutes
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.netty.EngineMain
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.resources.*
import io.ktor.server.routing.*

fun main(args: Array<String>) =
    EngineMain.main(args)

fun Application.module() {
    install(Resources)
    install(ContentNegotiation) { json() }

    routing {
        userRoutes()
        postRoutes()
        commentRoutes()
    }
}
