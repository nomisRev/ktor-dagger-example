package com.example.repository

import com.example.comments.CommentTable
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

class UserRepositoryTest {
    private val database = PostgresContainer.getDatabase()
    private val userRepository by lazy { UserRepository(database) }

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
    fun `test user repository CRUD operations`() {
        // Create a user
        val user = userRepository.create("testuser", "test@example.com")
        assertNotNull(user)
        assertEquals("testuser", user.username)
        assertEquals("test@example.com", user.email)

        // Get user by ID
        val retrievedUser = userRepository.getById(user.id)
        assertNotNull(retrievedUser)
        assertEquals(user.id, retrievedUser.id)
        assertEquals(user.username, retrievedUser.username)
        assertEquals(user.email, retrievedUser.email)

        // Update user
        val updated = userRepository.update(user.id, username = "updateduser", email = "updated@example.com")
        assertTrue(updated)

        // Get updated user
        val updatedUser = userRepository.getById(user.id)
        assertNotNull(updatedUser)
        assertEquals("updateduser", updatedUser.username)
        assertEquals("updated@example.com", updatedUser.email)

        // Delete user
        val deleted = userRepository.delete(user.id)
        assertTrue(deleted)

        // Verify user is deleted
        val deletedUser = userRepository.getById(user.id)
        assertNull(deletedUser)
    }
}
