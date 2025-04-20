package com.example.config

import com.example.comments.CommentRepository
import com.example.posts.PostRepository
import com.example.users.UserRepository
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.config.ApplicationConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.dsl.onClose

fun appModule(application: Application): Module = module {
    single<ApplicationConfig> { application.environment.config }

    single<CoroutineScope> {
        CoroutineScope(application.coroutineContext + Dispatchers.Default)
    }

    single<HikariDataSource> {
        val config = get<ApplicationConfig>()
        HikariDataSource(HikariConfig().apply {
            driverClassName = config.property("database.driverClassName").getString()
            jdbcUrl = config.property("database.jdbcUrl").getString()
            username = config.property("database.username").getString()
            password = config.property("database.password").getString()
            validate()
        })
    } onClose { it?.close() }

    single<Database> {
        val dataSource = get<HikariDataSource>()
        Database.connect(dataSource)
    } onClose { it?.let { TransactionManager.closeAndUnregister(it) } }

    single { UserRepository(get()) }
    single { PostRepository(get()) }
    single { CommentRepository(get()) }
}