package com.example.users

import io.ktor.http.*
import io.ktor.resources.Resource
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
@Resource("/users")
class UsersResource {
    @Resource("{id}")
    class Id(val parent: UsersResource = UsersResource(), val id: Long)

    @Resource("{id}/posts")
    class Posts(val parent: Id)
}

fun Application.userRoutes(userRepository: UserRepository) = routing {
    get<UsersResource> {
        call.respond(userRepository.getAll())
    }

    get<UsersResource.Id> { route ->
        val user = userRepository.getById(route.id)
        if (user != null) call.respond(user)
        else call.respond(HttpStatusCode.NotFound, "User not found")
    }

    post<UsersResource> {
        val userParams = call.receive<Map<String, String>>()
        val username = userParams["username"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Username is required")
        val email = userParams["email"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Email is required")

        val user = userRepository.create(username, email)
        call.respond(HttpStatusCode.Created, user)
    }
}
