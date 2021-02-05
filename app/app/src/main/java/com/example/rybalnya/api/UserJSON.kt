package com.example.rybalnya.api

data class UserJSON(
    var Id: Int?,
    var About: String?,
    var Email: String?,
    var FullName: String?,
    var Image: ByteArray?,
    var Nickname: String?,
    var Password: String?
)