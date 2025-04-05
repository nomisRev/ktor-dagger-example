package com.example

import dagger.Component
import io.ktor.server.application.*
import javax.inject.Inject
import javax.inject.Singleton

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val test = Test()
    val iNeedTest = INeedTest(test)

    val component = DaggerApplicationComponent.create()
    configureRouting()
}

@Singleton
@Component
interface ApplicationComponent {
    fun needTest(): INeedTest
}

@Singleton
class Test @Inject constructor() {
    fun hello() = "Hello"
}

@Singleton
class INeedTest @Inject constructor(private val test: Test) {
    fun test() = test.hello()
}
