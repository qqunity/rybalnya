package com.example.rybalnya.api

data class CommentJSON(
    val ID: Int?,
    val UserID: Int?,
    val PostID: Int?,
    val Content: String?,
    val WasUpdated: Boolean?,
    val CreatedAt: String?
)