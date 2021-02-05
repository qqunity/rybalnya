package com.example.rybalnya.ui.profile

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rybalnya.BASE_URL
import com.example.rybalnya.MainActivity2
import com.example.rybalnya.R
import com.example.rybalnya.adapters.PhotocardAdapter
import com.example.rybalnya.api.ApiRequests
import com.example.rybalnya.api.PostRecieve
import com.example.rybalnya.models.PostModel
import com.example.rybalnya.ui.chat.MessageActivity
import com.google.android.material.appbar.MaterialToolbar
import com.ms.square.android.expandabletextview.ExpandableTextView
import kotlinx.android.synthetic.main.activity_another_user_profile.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.awaitResponse
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.properties.Delegates

class AnotherUserProfileActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var photocardAdapter: PhotocardAdapter
    private lateinit var postList: ArrayList<PostModel>
    private lateinit var postRecieve: ArrayList<PostRecieve>
    private var userId by Delegates.notNull<Int>()
    private var isFriend: Boolean = false
    private lateinit var userName: String
    private lateinit var userMail: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_another_user_profile)

        recyclerView = findViewById(R.id.recycler_view_user_pictures)
        recyclerView.setHasFixedSize(true)
        val mLayoutManager: LinearLayoutManager = GridLayoutManager(this, 3)
        recyclerView.layoutManager = mLayoutManager
        val itemDecoration = PostOffsetDecoration(this, R.dimen.item_offset)
        recyclerView.addItemDecoration(itemDecoration)
        postList = ArrayList()
        photocardAdapter = PhotocardAdapter(this, postList)
        recyclerView.adapter = photocardAdapter

        topAppBar.setNavigationOnClickListener {
            onBackPressed()
        }

        userId = intent.getIntExtra("userId", 0)

        getUserInfo(
            topAppBar,
            usr_profile_image,
            user_nick,
            user_fullname,
            expandable_text,
            userId,
            MainActivity2().getTkn().toString()
        )

        //TODO("Check if this user is a friend of current user")

        write_msg.setOnClickListener {
            val intent = Intent(this, MessageActivity::class.java)
            intent.putExtra("userEmail", userMail)
            //TODO("Wait for Oleg to finish chat")
            //intent.putExtra("userImg", user.imageStr)
            intent.putExtra("userName", userName)
            this.startActivity(intent)
        }

        if (isFriend) {
            add_del_friend.text = "Удалить из друзей"
//            add_del_friend.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, ))
            add_del_friend.setTextColor(Color.argb(255, 227, 38, 54))
        }

        getUserPosts(userId, MainActivity2().getTkn().toString())

    }

    private fun getUserInfo(
        bar: MaterialToolbar,
        imageView: ImageView,
        username: TextView,
        userFullname: TextView,
        userBio: TextView,
        userId: Int,
        token: String
    ) {
        var api: ApiRequests? = null
        try {
            api = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiRequests::class.java)
        } catch (e: Exception) {
            Log.i("ApiRequest", e.toString())
        }
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val response = api?.getUserById(userId, token)?.awaitResponse()
                Log.i("Response", response.toString())
                Log.i("Response body", response?.body().toString())
                if (response?.code() == 200) {
                    Log.i("getPosts", "Successful")
                    if (response.body()?.UserInfo?.Image != null) {
                        val bmp = Base64.decode(response.body()?.UserInfo!!.Image, 0)
                        imageView.setImageBitmap(BitmapFactory.decodeByteArray(bmp, 0, bmp.size))
                    } else {
                        imageView.setImageResource(R.mipmap.ic_launcher)
                    }
                    userMail = response.body()?.UserInfo!!.Email.toString()
                    val LLmid = userFullname.parent as LinearLayout
                    if (response.body()?.UserInfo!!.FullName != "" || response.body()?.UserInfo!!.FullName != null) {
                        LLmid.visibility = View.VISIBLE
                        userFullname.visibility = View.VISIBLE
                        userFullname.text = response.body()!!.UserInfo!!.FullName
                    }
                    if (response.body()?.UserInfo!!.Nickname != "" || response.body()?.UserInfo!!.Nickname != null) {
                        username.text = response.body()!!.UserInfo!!.Nickname
                    } else {
                        username.text = response.body()!!.UserInfo!!.Email
                    }
                    bar.title = username.text.toString()
                    userName = username.text.toString()
                    if (response.body()?.UserInfo?.About != null) {
                        LLmid.visibility = View.VISIBLE
                        val expTxt = userBio.parent as ExpandableTextView
                        expTxt.visibility = View.VISIBLE
                        userBio.visibility = View.VISIBLE
                        userBio.text = response.body()!!.UserInfo!!.About
                        expTxt.text = response.body()!!.UserInfo!!.About
                    }
                } else if (response?.code() == 400 || response?.code() == 401) {
                    Log.i("getUserInfo", "Something went wrong")
                    Toast.makeText(
                        this@AnotherUserProfileActivity,
                        response.body()?.Error.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.i("Упс...", e.toString())
            }
        }


    }

    private fun getUserPosts(userId: Int, token: String) {
        var api: ApiRequests? = null
        try {
            api = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiRequests::class.java)
        } catch (e: Exception) {
            Log.i("ApiRequest", e.toString())
        }
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val response = api?.getAnotherUserPosts(userId, token)?.awaitResponse()

                Log.i("Response", response.toString())
                Log.i("Response body", response?.body().toString())
                if (response?.code() == 200) {
                    Log.i("getPosts", "Successful")
                    postRecieve = ArrayList()
                    if (response.body()?.Posts != null) {
                        postRecieve = response.body()!!.Posts
                    } else {
                        postRecieve.clear()
                        Toast.makeText(
                            this@AnotherUserProfileActivity,
                            "Посты не найдены",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    val res = ArrayList<PostModel>()

                    for (post in postRecieve) {
                        res.add(
                            PostModel(
                                post.ID!!,
                                post.Image!!,
                                post.Description,
                                post.UserID!!,
                                post.CreatedAt!!
                            )
                        )
                    }

                    postList.clear()
                    postList.addAll(res)
                    postList.reverse()

                    photocardAdapter.notifyDataSetChanged()
/*                    swipeRefreshLayout.isRefreshing = false
                    isRefreshing = false
                    mLayoutManager.scrollToPosition(postList.size - 1)*/

                } else if (response?.code() == 400 || response?.code() == 401) {
                    Log.i("getPosts", "Something went wrong")
                }
            } catch (e: Exception) {
                Log.i("getPosts", e.toString())
            }
        }
    }

}