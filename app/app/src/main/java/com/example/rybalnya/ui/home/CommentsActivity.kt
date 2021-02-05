package com.example.rybalnya.ui.home

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.rybalnya.BASE_URL
import com.example.rybalnya.MainActivity2
import com.example.rybalnya.R
import com.example.rybalnya.adapters.CommentAdapter
import com.example.rybalnya.api.ApiRequests
import com.example.rybalnya.api.CommentJSON
import com.example.rybalnya.models.CommentModel
import kotlinx.android.synthetic.main.activity_comments.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.awaitResponse
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File


private var first: Boolean = true
private var isRefreshing: Boolean = false

class CommentsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var commentAdapter: CommentAdapter
    private lateinit var commentList: ArrayList<CommentModel>
    private lateinit var commRecieve: ArrayList<CommentJSON>
    private lateinit var addComment: EditText
    private lateinit var imageProfile: ImageView
    private lateinit var post: TextView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var mLayoutManager: LinearLayoutManager

    private var postid = 0
    private var publisherid = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)

        topAppBar.setNavigationOnClickListener {
            onBackPressed()
            finish()
        }

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_comment)

        val intent = intent
        postid = intent.getIntExtra("postid", 0)
        publisherid = intent.getIntExtra("publisherid", 0)

        recyclerView = findViewById(R.id.recycler_view)
        //recyclerView.setHasFixedSize(true)
        recyclerView.setItemViewCacheSize(100)
        mLayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = mLayoutManager
        commentList = ArrayList()
        commentAdapter = CommentAdapter(this, commentList)
        recyclerView.adapter = commentAdapter

        post = findViewById(R.id.post)
        addComment = findViewById(R.id.add_comment)
        imageProfile = findViewById(R.id.image_profile)

        post.setOnClickListener {
            if (addComment.text.toString() == "") {
                Toast.makeText(this, "Поле пустое", Toast.LENGTH_SHORT).show()
            } else {
                postComment(postid, addComment.text.toString(), MainActivity2().getTkn()!!)
                addComment.setText("")
            }
        }

        swipeRefreshLayout.setOnRefreshListener {
            if (!isRefreshing) {
                isRefreshing = true
                readComments(postid, MainActivity2().getTkn()!!)
            }
        }

        if (first) {
            readComments(postid, MainActivity2().getTkn()!!)
            first = false
        }

        if (File(this.filesDir.path.toString() + "/avatar.png").exists()) {
            imageProfile.setImageBitmap(BitmapFactory.decodeFile((this.filesDir.path.toString() + "/avatar.png")))
        } else {
            imageProfile.setImageResource(R.mipmap.ic_launcher)
        }

    }

    override fun onResume() {
        if (!isRefreshing) {
            isRefreshing = true
            readComments(postid, MainActivity2().getTkn()!!)
        }
        super.onResume()
    }

    private fun postComment(postid: Int, content: String, token: String) {
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
                val response =
                    api?.newComment(CommentJSON(null, null, postid, content, null, null), token)
                        ?.awaitResponse()
                Log.i("Response", response.toString())
                Log.i("Response body", response?.body().toString())
                if (response?.code() == 201) {
                    Log.i("postComment", "Successful")
                    Toast.makeText(
                        this@CommentsActivity,
                        response.body()?.Action.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                    readComments(postid, token)
                } else if (response?.code() == 400 || response?.code() == 401) {
                    Log.i("postComment", "Something went wrong")
                    Toast.makeText(
                        this@CommentsActivity,
                        response.body()?.Error.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.i("Упс...", e.toString())
            }
        }
    }

    private fun readComments(postid: Int, token: String) {
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
                val response = api?.getPostComments(postid, token)?.awaitResponse()

                Log.i("Response", response.toString())
                Log.i("Response body", response?.body().toString())
                if (response?.code() == 200) {
                    Log.i("getPosts", "Successful")
                    Toast.makeText(
                        this@CommentsActivity,
                        response.body()?.Action.toString(),
                        Toast.LENGTH_SHORT
                    ).show()

                    commRecieve = ArrayList()

                    if (response.body()?.Comments != null) {
                        commRecieve = response.body()!!.Comments
                    } else {
                        commRecieve.clear()
                        Toast.makeText(
                            this@CommentsActivity,
                            "Посты не найдены",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    val res = ArrayList<CommentModel>()

                    for (comment in commRecieve) {
                        res.add(
                            CommentModel(
                                comment.Content!!,
                                comment.UserID!!,
                                comment.PostID!!,
                                comment.ID!!,
                                comment.CreatedAt!!
                            )
                        )
                    }

                    commentList.clear()
                    commentList.addAll(res)

                    commentAdapter.notifyDataSetChanged()
                    swipeRefreshLayout.isRefreshing = false
                    isRefreshing = false
                    mLayoutManager.scrollToPosition(0)

                } else if (response?.code() == 400 || response?.code() == 401) {
                    Log.i("getPosts", "Something went wrong")
                    Toast.makeText(
                        this@CommentsActivity,
                        response.body()?.Error.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.i("getPosts", e.toString())
            }
        }
    }
}