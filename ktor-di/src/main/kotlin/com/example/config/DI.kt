@file:JvmName("DI")
package com.example.config

import com.example.comments.CommentRepository
import com.example.posts.PostRepository
import com.example.users.UserRepository
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.plugins.di.annotations.Property
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.plugins.di.invoke
import io.ktor.server.plugins.di.provide
import io.ktor.server.plugins.di.resolve
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager


data class DbConfig(
    val driverClassName: String,
    val jdbcUrl: String,
    val username: String,
    val password: String
)

fun Application.di(@Property("database") config: DbConfig) {
    dependencies {
        provide { this@di.provideDatabase(config) }
        provide { UserRepository(resolve()) }
        provide { PostRepository(resolve()) }
        provide { CommentRepository(resolve()) }
    }
}

private fun Application.provideDatabase(config: DbConfig): Database {
    val dataSource = HikariDataSource(HikariConfig().apply {
        driverClassName = config.driverClassName
        jdbcUrl = config.jdbcUrl
        username = config.username
        password = config.password
        validate()
    })
    val database = Database.connect(dataSource)
    monitor.subscribe(ApplicationStopped) {
        TransactionManager.closeAndUnregister(database)
        dataSource.close()
    }
    return database
}
