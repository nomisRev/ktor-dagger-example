package com.example.comments

import io.ktor.http.*
import io.ktor.resources.Resource
import io.ktor.server.application.*
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject
import kotlin.getValue

@Serializable
@Resource("/comments")
class CommentsResource {
    @Resource("{id}")
    class Id(val parent: CommentsResource = CommentsResource(), val id: Long)

    @Resource("/")
    class Create(
        val parent: CommentsResource = CommentsResource(),
        val postId: Long,
        val userId: Long,
        val content: String
    )

    @Resource("post/{postId}")
    class ByPost(val parent: CommentsResource = CommentsResource(), val postId: Long)

    @Resource("user/{userId}")
    class ByUser(val parent: CommentsResource = CommentsResource(), val userId: Long)
}

fun Application.commentRoutes() = routing {
    val commentRepository by inject<CommentRepository>()
    get<CommentsResource> {
        call.respond(commentRepository.getAll())
    }

    get<CommentsResource.Id> { route ->
        val comment = commentRepository.getById(route.id)
        if (comment != null) call.respond(comment)
        else call.respond(HttpStatusCode.NotFound, "Comment not found")
    }

    get<CommentsResource.ByPost> { route ->
        call.respond(commentRepository.getByPostId(route.postId))
    }

    get<CommentsResource.ByUser> { route ->
        call.respond(commentRepository.getByUserId(route.userId))
    }

    post<CommentsResource.Create> { create ->
        val comment = commentRepository.create(create.postId, create.userId, create.content)
        call.respond(HttpStatusCode.Created, comment)
    }
}