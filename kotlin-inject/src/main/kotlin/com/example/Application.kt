package com.example

import com.example.comments.CommentRepository
import com.example.comments.commentRoutes
import com.example.config.AppComponent
import com.example.config.create
import com.example.posts.PostRepository
import com.example.posts.postRoutes
import com.example.users.UserRepository
import com.example.users.userRoutes
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.resources.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.Database

fun main(args: Array<String>) =
    io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    // Create the component with the application instance
    val component = AppComponent::class.create(this)

    install(Resources)
    install(ContentNegotiation) { json() }

    routing {
        userRoutes(component.userRepository)
        postRoutes(component.postRepository)
        commentRoutes(component.commentRepository)
    }
}
