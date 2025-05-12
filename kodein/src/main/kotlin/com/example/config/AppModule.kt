package com.example.config

import com.example.comments.CommentRepository
import com.example.posts.PostRepository
import com.example.users.UserRepository
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopping
import io.ktor.server.config.ApplicationConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton
import org.kodein.di.bindings.WeakContextScope
import org.kodein.di.scoped
import org.kodein.di.with

fun appModule(application: Application) = DI.Module("app") {
    bind<ApplicationConfig>() with singleton { application.environment.config }

    bind<CoroutineScope>() with singleton {
        CoroutineScope(application.coroutineContext + Dispatchers.Default)
    }

    bind<HikariDataSource>() with scoped(WeakContextScope.of<Application>()).singleton {
        val config = instance<ApplicationConfig>()
        HikariDataSource(HikariConfig().apply {
            driverClassName = config.property("database.driverClassName").getString()
            jdbcUrl = config.property("database.jdbcUrl").getString()
            username = config.property("database.username").getString()
            password = config.property("database.password").getString()
            validate()
        })
    }

    bind<Database>() with singleton {
        val dataSource = instance<HikariDataSource>()
        Database.connect(dataSource)
    }

    // Register shutdown hook to close resources
    application.environment.monitor.subscribe(ApplicationStopping) {
        // Resources will be closed automatically when the application stops
    }

    bind<UserRepository>() with singleton { UserRepository(instance()) }
    bind<PostRepository>() with singleton { PostRepository(instance()) }
    bind<CommentRepository>() with singleton { CommentRepository(instance()) }
}
