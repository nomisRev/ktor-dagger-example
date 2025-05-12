package com.example.posts

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

object PostTable : LongIdTable("posts", "post_id") {
    val userId = long("user_id")
    val title = varchar("title", 100)
    val content = text("content")
    val createdAt = long("created_at")
}

class PostRepository(private val database: Database) {

    fun create(userId: Long, title: String, content: String): Post = transaction(database) {
        val createdAt = System.currentTimeMillis()

        val postId = PostTable.insert {
            it[PostTable.userId] = userId
            it[PostTable.title] = title
            it[PostTable.content] = content
            it[PostTable.createdAt] = createdAt
        } get PostTable.id

        Post(postId.value, userId, title, content, createdAt)
    }

    fun getById(id: Long): Post? = transaction(database) {
        PostTable.selectAll()
            .where { PostTable.id eq id }
            .map { it.toPost() }
            .singleOrNull()
    }

    fun getByUserId(userId: Long): List<Post> = transaction(database) {
        PostTable.selectAll()
            .where { PostTable.userId eq userId }
            .map { it.toPost() }
    }

    fun getAll(): List<Post> = transaction(database) {
        PostTable.selectAll()
            .map { it.toPost() }
    }

    fun update(id: Long, title: String? = null, content: String? = null): Boolean = transaction(database) {
        PostTable.update({ PostTable.id eq id }) { stmt ->
            title?.let { stmt[PostTable.title] = it }
            content?.let { stmt[PostTable.content] = it }
        } > 0
    }

    fun delete(id: Long): Boolean = transaction(database) {
        PostTable.deleteWhere { PostTable.id eq id } > 0
    }

    private fun ResultRow.toPost(): Post = Post(
        id = this[PostTable.id].value,
        userId = this[PostTable.userId],
        title = this[PostTable.title],
        content = this[PostTable.content],
        createdAt = this[PostTable.createdAt]
    )
}