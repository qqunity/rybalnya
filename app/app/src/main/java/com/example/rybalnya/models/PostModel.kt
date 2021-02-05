package com.example.rybalnya.models

class PostModel(
    var postId: Int,
    var postImage: String,
    var description: String?,
    var publisher: Int,
    var createdAt: String
) {
    override fun equals(other: Any?): Boolean = (other is PostModel)
            && postId == other.postId
            && postImage == other.postImage
            && description == other.description
            && publisher == other.publisher
            && createdAt == other.createdAt
}