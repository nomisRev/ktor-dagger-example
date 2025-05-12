package com.example.posts

import io.ktor.http.*
import io.ktor.resources.Resource
import io.ktor.server.application.*
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI

@Serializable
@Resource("/posts")
class PostsResource {
    @Resource("{id}")
    class Id(val parent: PostsResource = PostsResource(), val id: Long)

    @Resource("/")
    class Create(
        val parent: PostsResource = PostsResource(),
        val userId: Long,
        val title: String,
        val content: String
    )

    @Resource("user/{userId}")
    class ByUser(val parent: PostsResource = PostsResource(), val userId: Long)
}

fun Application.postRoutes() = routing {
    val postRepository by closestDI().instance<PostRepository>()

    get<PostsResource> {

        call.respond(postRepository.getAll())
    }

    get<PostsResource.Id> { route ->
        val post = postRepository.getById(route.id)
        if (post != null) call.respond(post)
        else call.respond(HttpStatusCode.NotFound, "Post not found")
    }

    get<PostsResource.ByUser> { route ->
        call.respond(postRepository.getByUserId(route.userId))
    }

    post<PostsResource.Create> { create ->
        val post = postRepository.create(create.userId, create.title, create.content)
        call.respond(HttpStatusCode.Created, post)
    }
}