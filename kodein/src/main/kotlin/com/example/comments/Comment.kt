package com.example.comments

import kotlinx.serialization.Serializable

@Serializable
data class Comment(
    val id: Long,
    val postId: Long,
    val userId: Long,
    val content: String,
    val createdAt: Long
)