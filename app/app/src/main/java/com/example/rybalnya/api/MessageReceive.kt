package com.example.rybalnya.api

data class MessageReceive(
    val ID: Int?,
    var UserID: Int?,
    var ChatID: Int?,
    var Message: String?,
    var IsEdited: Boolean?,
    var IsSeen: Boolean?,
    var CreatedAt: String?
)
