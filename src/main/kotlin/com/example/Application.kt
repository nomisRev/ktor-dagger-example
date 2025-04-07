package com.example

import com.example.config.AppModule
import com.example.config.DaggerApplicationComponent
import com.example.features.comments.api.commentRoutes
import com.example.posts.postRoutes
import com.example.users.userRoutes
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.resources.*
import io.ktor.server.routing.*

fun main(args: Array<String>) =
    io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    val component = DaggerApplicationComponent.builder()
        .appModule(AppModule(this))
        .build()

    install(Resources)
    install(ContentNegotiation) { json() }

    routing {
        userRoutes(component.userRepository())
        postRoutes(component.postRepository())
        commentRoutes(component.commentRepository())
    }
}
