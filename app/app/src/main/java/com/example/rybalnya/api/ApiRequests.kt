package com.example.rybalnya.api

import com.example.rybalnya.WEATHER_API_KEY
import com.example.rybalnya.api.weather.ForecastJSON
import retrofit2.Call
import retrofit2.http.*

interface ApiRequests {

    @POST("/add_user_info")
    fun signUpUser(@Body user: UserJSON): Call<ResponseJSON>

    @POST("/login")
    fun loginUser(@Body user: UserJSON): Call<ResponseJSON>

    @GET("/get_user_info")
    fun getUserInfoByEmail(
        @Query("email") userMail: String,
        @Header("Access-Token") token: String
    ): Call<ResponseJSON>

    @PUT("/update_user_info")
    fun updateUser(
        @Body user: UserRecieve,
        @Header("Access-Token") token: String
    ): Call<ResponseJSON>

    @DELETE("/delete_user_info")
    fun deleteUserByEmail(
        @Query("email") userMail: String,
        @Header("Access-Token") token: String
    ): Call<ResponseJSON>

    @POST("/add_post")
    fun createPost(
        @Body post: PostRecieve,
        @Header("Access-Token") token: String
    ): Call<ResponseJSON>

    @GET("/get_all_posts")
    fun getAllPosts(
        @Header("Access-Token") token: String
    ): Call<ResponseJSON>

    @GET("/get_all_posts_with_token")
    fun getCurrentUserPosts(
        @Header("Access-Token") token: String
    ): Call<ResponseJSON>

    @GET("/get_posts")
    fun getAnotherUserPosts(
        @Query("user_id") userId: Int,
        @Header("Access-Token") token: String
    ): Call<ResponseJSON>

    @DELETE("/delete_post")
    fun deletePost(
        @Query("id") postId: Int,
        @Header("Access-Token") token: String
    ): Call<ResponseJSON>

    @PUT("/update_post")
    fun editPostDescription(
        @Query("id") postId: Int,
        @Body post: PostRecieve,
        @Header("Access-Token") token: String
    ): Call<ResponseJSON>

    @POST("/add_like")
    fun addLike(
        @Query("post_id") postId: Int,
        @Header("Access-Token") token: String
    ): Call<ResponseJSON>

    @DELETE("/delete_like")
    fun deleteLike(
        @Query("post_id") postId: Int,
        @Header("Access-Token") token: String
    ): Call<ResponseJSON>

    @GET("/is_liked")
    fun isLiked(
        @Query("post_id") postId: Int,
        @Header("Access-Token") token: String
    ): Call<ResponseJSON>

    @GET("/get_count_of_likes")
    fun countLikes(
        @Query("post_id") postId: Int,
        @Header("Access-Token") token: String
    ): Call<ResponseJSON>

    @GET("/get_count_of_comments")
    fun countComments(
        @Query("post_id") postId: Int,
        @Header("Access-Token") token: String
    ): Call<ResponseJSON>

    @GET("/get_user_info")
    fun getUserById(
        @Query("user_id") userId: Int,
        @Header("Access-Token") token: String
    ): Call<ResponseJSON>

    @GET("/get_all_users_info")
    fun getAllUsersInfo(
        @Header("Access-Token") token: String
    ): Call<ResponseJSON>

    @GET("/get_all_users_info_with_online")
    fun getAllUsersInfoWithOnline(
        @Header("Access-Token") token: String
    ): Call<ResponseJSON>

    @GET("/get_msgs_by_roomname")
    fun getMsgs(
        @Query("roomname") roomName: String,
        @Header("Access-Token") token: String
    ): Call<ResponseJSON>

    @POST("/add_comment")
    fun newComment(
        @Body comment: CommentJSON,
        @Header("Access-Token") token: String
    ): Call<ResponseJSON>

    @GET("/get_comments")
    fun getPostComments(
        @Query("post_id") postId: Int,
        @Header("Access-Token") token: String
    ): Call<ResponseJSON>

    @DELETE("/delete_comment")
    fun deleteComment(
        @Query("id") commentId: Int,
        @Header("Access-Token") token: String
    ): Call<ResponseJSON>

    @PUT("/update_comment")
    fun editComment(
        @Query("id") commentId: Int,
        @Body comment: CommentJSON,
        @Header("Access-Token") token: String
    ): Call<ResponseJSON>

    @GET("forecast")
    fun getForecast(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "ru",
        @Query("APPID") apiKey: String = WEATHER_API_KEY
    ): Call<ForecastJSON>

    @PUT("/user_online_true")
    fun updateOnlineTrue(
        @Query("Email") userEmail: String,
        @Header("Access-Token") token: String
    ): Call<ResponseJSON>

    @PUT("/user_online_false")
    fun updateOnlineFalse(
        @Query("Email") userEmail: String,
        @Header("Access-Token") token: String
    ): Call<ResponseJSON>

    @GET("/get_users_with_last_msgs")
    fun getUsersWithLastMsgs(
        @Query("email") email: String,
        @Header("Access-Token") token: String
    ): Call<ResponseJSON>
}