package com.example.rybalnya.api

data class ResponseJSON(
    val Action: String?,
    val Status: Boolean,
    val Error: String?,
    val UserID: Int?,
    val AccessToken: String?,
    val UserInfo: UserRecieve?,
    val Posts: ArrayList<PostRecieve>,
    val Comments: ArrayList<CommentJSON>,
    val IsTokenExpire: Boolean?,
    val CntLikes: Int?,
    val CntComments: Int?,
    val IsLiked: Boolean?,
    val Users: ArrayList<UserRecieve>?,
    val AllMsgs: ArrayList<MessageReceive>?,
    val Online: ArrayList<Boolean>?,
    val LastMsgs: ArrayList<MessageReceive>
)