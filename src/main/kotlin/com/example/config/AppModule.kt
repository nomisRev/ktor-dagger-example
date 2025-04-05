package com.example.config

import com.example.comments.CommentRepository
import com.example.posts.PostRepository
import com.example.users.UserRepository
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dagger.Component
import dagger.Module
import dagger.Provides
import io.ktor.server.application.Application
import io.ktor.server.config.ApplicationConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class])
interface ApplicationComponent {
    fun userRepository(): UserRepository
    fun postRepository(): PostRepository
    fun commentRepository(): CommentRepository
}

@Module
class AppModule(private val application: Application) {

    @Provides
    @Singleton
    fun config(): ApplicationConfig = application.environment.config

    @Provides
    @Singleton
    fun scope(): CoroutineScope =
        CoroutineScope(application.coroutineContext + Dispatchers.Default)

    @Provides
    @Singleton
    fun dataSource(): HikariDataSource =
        HikariDataSource(HikariConfig().apply {
            driverClassName = "org.postgresql.Driver"
            jdbcUrl = "jdbc:postgresql://localhost:5432/ktordagger"
            username = "postgres"
            password = "postgres"
            validate()
        })

    @Provides
    @Singleton
    fun database(dataSource: HikariDataSource): Database =
        Database.connect(dataSource)
}