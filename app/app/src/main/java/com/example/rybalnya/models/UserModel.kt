package com.example.rybalnya.models

class UserModel(
    var userName: String?,
    var nickName: String?,
    var eMail: String?,
    var imageStr: String?,
    var status: String?,
    var lastMsg: String? = null,
    var senderOfLastMsg: String? = null,
    var isSeen: Boolean? = false
)