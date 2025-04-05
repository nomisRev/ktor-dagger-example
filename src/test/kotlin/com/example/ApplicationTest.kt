package com.example

import com.example.comments.CommentRepository
import com.example.comments.CommentTable
import com.example.posts.PostRepository
import com.example.posts.PostTable
import com.example.users.UserRepository
import com.example.users.UserTable
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.ktor.sample.PostgresContainer
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DatabaseApplicationTest {
    private val database = PostgresContainer.getDatabase()
    private val userRepository by lazy { UserRepository(database) }
    private val postRepository by lazy { PostRepository(database) }
    private val commentRepository by lazy { CommentRepository(database) }

    @Before
    fun setup() {
        transaction(database) {
            SchemaUtils.create(UserTable, PostTable, CommentTable)
        }
    }

    @After
    fun tearDown() {
        transaction(database) {
            SchemaUtils.drop(CommentTable, PostTable, UserTable)
        }
    }

    private fun Application.testModule() {
        install(ContentNegotiation) { json() }

        routing {
            get("/users") { call.respond(userRepository.getAll()) }

            get("/users/{id}") {
                val id = call.parameters["id"]?.toLongOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid ID")

                val user = userRepository.getById(id)
                if (user != null) call.respond(user)
                else call.respond(HttpStatusCode.NotFound, "User not found")
            }

            post("/users") {
                val userParams = call.receive<Map<String, String>>()
                val username = userParams["username"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Username is required")
                val email = userParams["email"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Email is required")

                val user = userRepository.create(username.toString(), email.toString())
                call.respond(HttpStatusCode.Created, user)
            }

            get("/posts") {
                call.respond(postRepository.getAllWithUsers())
            }

            get("/posts/{id}") {
                val id = call.parameters["id"]?.toLongOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid ID")

                val post = postRepository.getByIdWithUser(id)
                if (post != null) call.respond(post)
                else call.respond(HttpStatusCode.NotFound, "Post not found")
            }

            get("/users/{id}/posts") {
                val id = call.parameters["id"]?.toLongOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid ID")

                val posts = postRepository.getByUserId(id)
                call.respond(posts)
            }

            post("/posts") {
                val postParams = call.receive<Map<String, String>>()
                val userId = postParams["userId"]?.toLongOrNull()
                    ?: return@post call.respond(HttpStatusCode.BadRequest, "Valid userId is required")
                val title = postParams["title"] 
                    ?: return@post call.respond(HttpStatusCode.BadRequest, "Title is required")
                val content = postParams["content"] 
                    ?: return@post call.respond(HttpStatusCode.BadRequest, "Content is required")

                val post = postRepository.create(userId, title.toString(), content.toString())
                call.respond(HttpStatusCode.Created, post)
            }

            // Comment routes
            get("/posts/{id}/comments") {
                val id = call.parameters["id"]?.toLongOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid ID")

                val comments = commentRepository.getByPostIdWithUsers(id)
                call.respond(comments)
            }

            post("/posts/{id}/comments") {
                val id = call.parameters["id"]?.toLongOrNull()
                    ?: return@post call.respond(HttpStatusCode.BadRequest, "Invalid ID")

                val commentParams = call.receive<Map<String, String>>()
                val userId = commentParams["userId"]?.toLongOrNull()
                    ?: return@post call.respond(HttpStatusCode.BadRequest, "Valid userId is required")
                val content = commentParams["content"] 
                    ?: return@post call.respond(HttpStatusCode.BadRequest, "Content is required")

                val comment = commentRepository.create(id, userId, content.toString())
                call.respond(HttpStatusCode.Created, comment)
            }
        }
    }

    @Test
    fun `test user endpoints`() = testApplication {
        application {
            testModule()
        }

        // Create a user
        val createResponse = client.post("/users") {
            contentType(ContentType.Application.Json)
            setBody("""{"username": "testuser", "email": "test@example.com"}""")
        }
        assertEquals(HttpStatusCode.Created, createResponse.status)
        val createResponseText = createResponse.bodyAsText()
        assertTrue(createResponseText.contains("testuser"))
        assertTrue(createResponseText.contains("test@example.com"))

        // Extract user ID from response
        val userIdRegex = """"id":(\d+)""".toRegex()
        val userId = userIdRegex.find(createResponseText)?.groupValues?.get(1)?.toInt()
            ?: throw IllegalStateException("Could not extract user ID from response")

        // Get all users
        val getAllResponse = client.get("/users")
        assertEquals(HttpStatusCode.OK, getAllResponse.status)
        val getAllResponseText = getAllResponse.bodyAsText()
        assertTrue(getAllResponseText.contains("testuser"))

        // Get user by ID
        val getByIdResponse = client.get("/users/$userId")
        assertEquals(HttpStatusCode.OK, getByIdResponse.status)
        val getByIdResponseText = getByIdResponse.bodyAsText()
        assertTrue(getByIdResponseText.contains("testuser"))
        assertTrue(getByIdResponseText.contains("test@example.com"))
    }

    @Test
    fun `test post endpoints`() = testApplication {
        application { testModule() }

        val createUserResponse = client.post("/users") {
            contentType(ContentType.Application.Json)
            setBody("""{"username": "testuser", "email": "test@example.com"}""")
        }
        val createUserResponseText = createUserResponse.bodyAsText()
        val userIdRegex = """"id":(\d+)""".toRegex()
        val userId = userIdRegex.find(createUserResponseText)?.groupValues?.get(1)?.toInt()
            ?: throw IllegalStateException("Could not extract user ID from response")

        val createPostResponse = client.post("/posts") {
            contentType(ContentType.Application.Json)
            setBody("""{"userId": $userId, "title": "Test Post", "content": "This is a test post content"}""")
        }
        assertEquals(HttpStatusCode.Created, createPostResponse.status)
        val createPostResponseText = createPostResponse.bodyAsText()
        assertTrue(createPostResponseText.contains("Test Post"))
        assertTrue(createPostResponseText.contains("This is a test post content"))

        val postIdRegex = """"id":(\d+)""".toRegex()
        val postId = postIdRegex.find(createPostResponseText)?.groupValues?.get(1)?.toInt()
            ?: throw IllegalStateException("Could not extract post ID from response")

        val getAllPostsResponse = client.get("/posts")
        assertEquals(HttpStatusCode.OK, getAllPostsResponse.status)
        val getAllPostsResponseText = getAllPostsResponse.bodyAsText()
        assertTrue(getAllPostsResponseText.contains("Test Post"))

        val getPostByIdResponse = client.get("/posts/$postId")
        assertEquals(HttpStatusCode.OK, getPostByIdResponse.status)
        val getPostByIdResponseText = getPostByIdResponse.bodyAsText()
        assertTrue(getPostByIdResponseText.contains("Test Post"))
        assertTrue(getPostByIdResponseText.contains("This is a test post content"))

        val getPostsByUserResponse = client.get("/users/$userId/posts")
        assertEquals(HttpStatusCode.OK, getPostsByUserResponse.status)
        val getPostsByUserResponseText = getPostsByUserResponse.bodyAsText()
        assertTrue(getPostsByUserResponseText.contains("Test Post"))
    }

    @Test
    fun `test comment endpoints`() = testApplication {
        application {
            testModule()
        }

        // Create a user first
        val createUserResponse = client.post("/users") {
            contentType(ContentType.Application.Json)
            setBody("""{"username": "testuser", "email": "test@example.com"}""")
        }
        val createUserResponseText = createUserResponse.bodyAsText()
        val userIdRegex = """"id":(\d+)""".toRegex()
        val userId = userIdRegex.find(createUserResponseText)?.groupValues?.get(1)?.toInt()
            ?: throw IllegalStateException("Could not extract user ID from response")

        // Create a post
        val createPostResponse = client.post("/posts") {
            contentType(ContentType.Application.Json)
            setBody("""{"userId": $userId, "title": "Test Post", "content": "This is a test post content"}""")
        }
        val createPostResponseText = createPostResponse.bodyAsText()
        val postIdRegex = """"id":(\d+)""".toRegex()
        val postId = postIdRegex.find(createPostResponseText)?.groupValues?.get(1)?.toInt()
            ?: throw IllegalStateException("Could not extract post ID from response")

        // Create a comment
        val createCommentResponse = client.post("/posts/$postId/comments") {
            contentType(ContentType.Application.Json)
            setBody("""{"userId": $userId, "content": "This is a test comment"}""")
        }
        assertEquals(HttpStatusCode.Created, createCommentResponse.status)
        val createCommentResponseText = createCommentResponse.bodyAsText()
        assertTrue(createCommentResponseText.contains("This is a test comment"))

        // Get comments for post
        val getCommentsResponse = client.get("/posts/$postId/comments")
        assertEquals(HttpStatusCode.OK, getCommentsResponse.status)
        val getCommentsResponseText = getCommentsResponse.bodyAsText()
        assertTrue(getCommentsResponseText.contains("This is a test comment"))
    }
}
