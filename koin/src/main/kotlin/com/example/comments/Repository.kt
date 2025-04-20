package com.example.comments

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

object CommentTable : LongIdTable("comments", "comment_id") {
    val postId = long("post_id")
    val userId = long("user_id")
    val content = text("content")
    val createdAt = long("created_at")
}

class CommentRepository(private val database: Database) {

    fun create(postId: Long, userId: Long, content: String): Comment = transaction(database) {
        val createdAt = System.currentTimeMillis()

        val commentId = CommentTable.insert {
            it[CommentTable.postId] = postId
            it[CommentTable.userId] = userId
            it[CommentTable.content] = content
            it[CommentTable.createdAt] = createdAt
        } get CommentTable.id

        Comment(commentId.value, postId, userId, content, createdAt)
    }

    fun getById(id: Long): Comment? = transaction(database) {
        CommentTable.selectAll()
            .where { CommentTable.id eq id }
            .map { it.toComment() }
            .singleOrNull()
    }

    fun getByPostId(postId: Long): List<Comment> = transaction(database) {
        CommentTable.selectAll()
            .where { CommentTable.postId eq postId }
            .map { it.toComment() }
    }

    fun getByUserId(userId: Long): List<Comment> = transaction(database) {
        CommentTable.selectAll()
            .where { CommentTable.userId eq userId }
            .map { it.toComment() }
    }

    fun getAll(): List<Comment> = transaction(database) {
        CommentTable.selectAll()
            .map { it.toComment() }
    }

    fun update(id: Long, content: String): Boolean = transaction(database) {
        CommentTable.update({ CommentTable.id eq id }) { stmt ->
            stmt[CommentTable.content] = content
        } > 0
    }

    fun delete(id: Long): Boolean = transaction(database) {
        CommentTable.deleteWhere { CommentTable.id eq id } > 0
    }

    private fun ResultRow.toComment(): Comment = Comment(
        id = this[CommentTable.id].value,
        postId = this[CommentTable.postId],
        userId = this[CommentTable.userId],
        content = this[CommentTable.content],
        createdAt = this[CommentTable.createdAt]
    )
}