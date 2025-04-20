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
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Inject
import me.tatarka.inject.annotations.Provides
import me.tatarka.inject.annotations.Scope
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import kotlin.annotation.AnnotationRetention.RUNTIME

@Scope
@Retention(RUNTIME)
annotation class AppScope

@AppScope
@Component
abstract class AppComponent(private val application: Application) {
    abstract val userRepository: UserRepository
    abstract val postRepository: PostRepository
    abstract val commentRepository: CommentRepository

    @Provides
    fun config(): ApplicationConfig = application.environment.config

    @Provides
    fun scope(): CoroutineScope =
        CoroutineScope(application.coroutineContext + Dispatchers.Default)

    @Provides
    fun dataSource(config: ApplicationConfig): HikariDataSource =
        HikariDataSource(HikariConfig().apply {
            driverClassName = config.property("database.driverClassName").getString()
            jdbcUrl = config.property("database.jdbcUrl").getString()
            username = config.property("database.username").getString()
            password = config.property("database.password").getString()
            validate()
        })

    @Provides
    fun database(dataSource: HikariDataSource): Database =
        Database.connect(dataSource).also { database ->
            application.monitor.subscribe(ApplicationStopped) {
                TransactionManager.closeAndUnregister(database)
                dataSource.close()
            }
        }
}
