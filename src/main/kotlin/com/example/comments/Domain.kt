package com.example.features.comments.domain

import com.example.features.posts.domain.Post
import com.example.com.example.users.User
import kotlinx.serialization.Serializable

/**
 * Data class representing a Comment entity
 */
@Serializable
data class Comment(
    val id: Long,
    val postId: Long,
    val userId: Long,
    val content: String,
    val createdAt: Long
)

/**
 * Data class representing a Comment with User information
 */
@Serializable
data class CommentWithUser(
    val id: Long,
    val postId: Long,
    val content: String,
    val createdAt: Long,
    val user: User
)

/**
 * Data class representing a Comment with Post and User information
 */
@Serializable
data class CommentWithPostAndUser(
    val id: Long,
    val content: String,
    val createdAt: Long,
    val post: Post,
    val user: User
)