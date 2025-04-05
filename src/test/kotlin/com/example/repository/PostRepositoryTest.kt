package com.example.repository

import com.example.comments.CommentTable
import com.example.posts.PostRepository
import com.example.posts.PostTable
import com.example.users.UserRepository
import com.example.users.UserTable
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.ktor.sample.PostgresContainer
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PostRepositoryTest {
    private val database = PostgresContainer.getDatabase()
    private val userRepository by lazy { UserRepository(database) }
    private val postRepository by lazy { PostRepository(database) }

    @Before
    fun setup() {
        transaction(database) {
            SchemaUtils.create(UserTable, PostTable, CommentTable)
        }
    }

    @After
    fun tearDown() {
        // Drop tables
        transaction(database) {
            SchemaUtils.drop(CommentTable, PostTable, UserTable)
        }
    }

    @Test
    fun `test post repository CRUD operations`() {
        // Create a user first
        val user = userRepository.create("testuser", "test@example.com")

        // Create a post
        val post = postRepository.create(user.id, "Test Post", "This is a test post content")
        assertNotNull(post)
        assertEquals("Test Post", post.title)
        assertEquals("This is a test post content", post.content)
        assertEquals(user.id, post.userId)

        // Get post by ID
        val retrievedPost = postRepository.getById(post.id)
        assertNotNull(retrievedPost)
        assertEquals(post.id, retrievedPost.id)
        assertEquals(post.title, retrievedPost.title)
        assertEquals(post.content, retrievedPost.content)

        // Get post with user
        val postWithUser = postRepository.getByIdWithUser(post.id)
        assertNotNull(postWithUser)
        assertEquals(post.id, postWithUser.id)
        assertEquals(post.title, postWithUser.title)
        assertEquals(post.content, postWithUser.content)
        assertEquals(user.id, postWithUser.user.id)
        assertEquals(user.username, postWithUser.user.username)
        assertEquals(user.email, postWithUser.user.email)

        // Update post
        val updated = postRepository.update(post.id, title = "Updated Post", content = "Updated content")
        assertTrue(updated)

        // Get updated post
        val updatedPost = postRepository.getById(post.id)
        assertNotNull(updatedPost)
        assertEquals("Updated Post", updatedPost.title)
        assertEquals("Updated content", updatedPost.content)

        // Delete post
        val deleted = postRepository.delete(post.id)
        assertTrue(deleted)

        // Verify post is deleted
        val deletedPost = postRepository.getById(post.id)
        assertNull(deletedPost)
    }

    @Test
    fun `test getting posts by user`() {
        // Create users
        val user1 = userRepository.create("user1", "user1@example.com")
        val user2 = userRepository.create("user2", "user2@example.com")

        // Create posts
        val post1 = postRepository.create(user1.id, "Post by User 1", "Content of post by user 1")
        val post2 = postRepository.create(user2.id, "Post by User 2", "Content of post by user 2")

        // Test getting posts by user
        val user1Posts = postRepository.getByUserId(user1.id)
        assertEquals(1, user1Posts.size)
        assertEquals(post1.id, user1Posts[0].id)

        val user2Posts = postRepository.getByUserId(user2.id)
        assertEquals(1, user2Posts.size)
        assertEquals(post2.id, user2Posts[0].id)
    }
}
