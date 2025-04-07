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
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.config.ApplicationConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
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
    fun dataSource(config: ApplicationConfig): HikariDataSource =
        HikariDataSource(HikariConfig().apply {
            driverClassName = config.property("database.driverClassName").getString()
            jdbcUrl = config.property("database.jdbcUrl").getString()
            username = config.property("database.username").getString()
            password = config.property("database.password").getString()
            validate()
        })

    @Provides
    @Singleton
    fun database(dataSource: HikariDataSource): Database =
        Database.connect(dataSource).also { database ->
            application.monitor.subscribe(ApplicationStopped) {
                TransactionManager.closeAndUnregister(database)
                dataSource.close()
            }
        }
}