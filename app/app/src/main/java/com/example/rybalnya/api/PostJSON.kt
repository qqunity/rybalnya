package com.example.rybalnya.api

data class PostJSON(
    val Description: String?,
    val ID: Int?,
    var Image: ByteArray,
    var UserID: Int?,
    var CreatedAt: String?
)