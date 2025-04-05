package com.example.repository

import com.example.comments.CommentRepository
import com.example.comments.CommentTable
import com.example.posts.PostRepository
import com.example.posts.PostTable
import com.example.posts.PostsResource.Comments
import com.example.users.UserRepository
import com.example.users.UserTable
import com.example.users.UsersResource.Posts
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

class CommentRepositoryTest {
    private val database = PostgresContainer.getDatabase()
    private lateinit var userRepository: UserRepository
    private lateinit var postRepository: PostRepository
    private lateinit var commentRepository: CommentRepository

    @Before
    fun setup() {
        // Create repositories
        userRepository = UserRepository(database)
        postRepository = PostRepository(database)
        commentRepository = CommentRepository(database)

        transaction(database) {
            SchemaUtils.create(UserTable, PostTable, CommentTable)
        }
    }

    @After
    fun tearDown() {
        // Drop tables
        transaction(database) {
            SchemaUtils.drop(UserTable, PostTable, CommentTable)
        }
    }

    @Test
    fun `test comment repository CRUD operations`() {
        // Create a user and post first
        val user = userRepository.create("testuser", "test@example.com")
        val post = postRepository.create(user.id, "Test Post", "This is a test post content")

        // Create a comment
        val comment = commentRepository.create(post.id, user.id, "This is a test comment")
        assertNotNull(comment)
        assertEquals("This is a test comment", comment.content)
        assertEquals(post.id, comment.postId)
        assertEquals(user.id, comment.userId)

        // Get comment by ID
        val retrievedComment = commentRepository.getById(comment.id)
        assertNotNull(retrievedComment)
        assertEquals(comment.id, retrievedComment.id)
        assertEquals(comment.content, retrievedComment.content)
        assertEquals(comment.postId, retrievedComment.postId)
        assertEquals(comment.userId, retrievedComment.userId)

        // Get comment with user
        val commentWithUser = commentRepository.getByIdWithUser(comment.id)
        assertNotNull(commentWithUser)
        assertEquals(comment.id, commentWithUser.id)
        assertEquals(comment.content, commentWithUser.content)
        assertEquals(comment.postId, commentWithUser.postId)
        assertEquals(user.id, commentWithUser.user.id)
        assertEquals(user.username, commentWithUser.user.username)
        assertEquals(user.email, commentWithUser.user.email)

        // Get comment with post and user
        val commentWithPostAndUser = commentRepository.getByIdWithPostAndUser(comment.id)
        assertNotNull(commentWithPostAndUser)
        assertEquals(comment.id, commentWithPostAndUser.id)
        assertEquals(comment.content, commentWithPostAndUser.content)
        assertEquals(post.id, commentWithPostAndUser.post.id)
        assertEquals(post.title, commentWithPostAndUser.post.title)
        assertEquals(post.content, commentWithPostAndUser.post.content)
        assertEquals(user.id, commentWithPostAndUser.user.id)
        assertEquals(user.username, commentWithPostAndUser.user.username)
        assertEquals(user.email, commentWithPostAndUser.user.email)

        // Delete comment
        val deleted = commentRepository.delete(comment.id)
        assertTrue(deleted)

        // Verify comment is deleted
        val deletedComment = commentRepository.getById(comment.id)
        assertNull(deletedComment)
    }

    @Test
    fun `test comment relationships`() {
        // Create users
        val user1 = userRepository.create("user1", "user1@example.com")
        val user2 = userRepository.create("user2", "user2@example.com")

        // Create posts
        val post1 = postRepository.create(user1.id, "Post by User 1", "Content of post by user 1")
        val post2 = postRepository.create(user2.id, "Post by User 2", "Content of post by user 2")

        // Create comments
        val comment1 = commentRepository.create(post1.id, user2.id, "Comment by User 2 on Post 1")
        val comment2 = commentRepository.create(post2.id, user1.id, "Comment by User 1 on Post 2")
        val comment3 = commentRepository.create(post1.id, user1.id, "Comment by User 1 on their own Post 1")

        // Test getting comments by post
        val post1Comments = commentRepository.getByPostId(post1.id)
        assertEquals(2, post1Comments.size)
        assertTrue(post1Comments.any { it.id == comment1.id })
        assertTrue(post1Comments.any { it.id == comment3.id })

        val post2Comments = commentRepository.getByPostId(post2.id)
        assertEquals(1, post2Comments.size)
        assertEquals(comment2.id, post2Comments[0].id)

        // Test getting comments by user
        val user1Comments = commentRepository.getByUserId(user1.id)
        assertEquals(2, user1Comments.size)
        assertTrue(user1Comments.any { it.id == comment2.id })
        assertTrue(user1Comments.any { it.id == comment3.id })

        val user2Comments = commentRepository.getByUserId(user2.id)
        assertEquals(1, user2Comments.size)
        assertEquals(comment1.id, user2Comments[0].id)

        // Test getting comments by post with user information
        val post1CommentsWithUsers = commentRepository.getByPostIdWithUsers(post1.id)
        assertEquals(2, post1CommentsWithUsers.size)
        val comment1WithUser = post1CommentsWithUsers.find { it.id == comment1.id }
        assertNotNull(comment1WithUser)
        assertEquals(user2.id, comment1WithUser.user.id)
        assertEquals(user2.username, comment1WithUser.user.username)

        // Test getting comments by user with post information
        val user1CommentsWithPosts = commentRepository.getByUserIdWithPosts(user1.id)
        assertEquals(2, user1CommentsWithPosts.size)
        val comment2WithPost = user1CommentsWithPosts.find { it.id == comment2.id }
        assertNotNull(comment2WithPost)
        assertEquals(post2.id, comment2WithPost.post.id)
        assertEquals(post2.title, comment2WithPost.post.title)
    }
}