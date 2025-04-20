package com.example

import com.example.comments.commentRoutes
import com.example.config.appModule
import com.example.posts.postRoutes
import com.example.users.userRoutes
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.resources.*
import io.ktor.server.routing.*
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun main(args: Array<String>) =
    io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    install(Koin) {
        slf4jLogger()
        modules(appModule(this@module))
    }

    install(Resources)
    install(ContentNegotiation) { json() }

    routing {
        userRoutes()
        postRoutes()
        commentRoutes()
    }
}
