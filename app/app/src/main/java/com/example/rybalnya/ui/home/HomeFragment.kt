package com.example.rybalnya.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.rybalnya.BASE_URL
import com.example.rybalnya.COMMENT_QUANTITY_CHANGED
import com.example.rybalnya.MainActivity2
import com.example.rybalnya.R
import com.example.rybalnya.adapters.PostAdapter
import com.example.rybalnya.api.ApiRequests
import com.example.rybalnya.api.PostRecieve
import com.example.rybalnya.models.PostModel
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.awaitResponse
import retrofit2.converter.gson.GsonConverterFactory


private lateinit var USER_TKN: String
private var isRefreshing: Boolean = false
private var clickedPost: Int = -1

class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var postAdapter: PostAdapter
    private lateinit var postList: ArrayList<PostModel>
    private lateinit var postRecieve: ArrayList<PostRecieve>
    private lateinit var mLayoutManager: LinearLayoutManager

    //private lateinit var recyclerView_story: RecyclerView
    //private lateinit var storyAdapter: StoryAdapter
    //private lateinit var storyList: List<Story>

    private lateinit var followingList: ArrayList<String>

    private lateinit var progressCircular: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        USER_TKN = MainActivity2().getTkn().toString()

        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh)

        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.setHasFixedSize(true)
        recyclerView.setItemViewCacheSize(100)
        mLayoutManager = LinearLayoutManager(context)
        //mLayoutManager.reverseLayout = true
        //mLayoutManager.stackFromEnd = true
        mLayoutManager.initialPrefetchItemCount = 25
        //recyclerView.addItemDecoration(DividerItemDecoration(this.requireActivity(), HORIZONTAL))
        recyclerView.layoutManager = mLayoutManager
        postList = ArrayList()
        postAdapter = PostAdapter(this.requireContext(), postList)
        postAdapter.setHasStableIds(true)
        recyclerView.adapter = postAdapter

        progressCircular = view.findViewById(R.id.progress_circular)

        swipeRefreshLayout.setOnRefreshListener {
            if (!isRefreshing) {
                isRefreshing = true
                getPosts(USER_TKN)
            }
        }

        getPosts(USER_TKN)

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy < -250 && scrl_to_start.visibility == View.GONE) {
                    scrl_to_start.show()
                    scrl_to_start.setOnClickListener {
                        recyclerView.smoothScrollToPosition(0)
                    }
                } else if (dy > 0 && scrl_to_start.visibility == View.VISIBLE || !recyclerView.canScrollVertically(
                        -1
                    )
                ) {
                    scrl_to_start.hide()
                    scrl_to_start.setOnClickListener {}
                }
                super.onScrolled(recyclerView, dx, dy)
            }
        })

        return view
    }

    private fun getPosts(token: String) {
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
                val response = api?.getAllPosts(token)?.awaitResponse()

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
                            this@HomeFragment.requireActivity(),
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
                    postRecieve.clear()

/*                    for (i in 0 until res.size){
                        if(i<postList.size) {
                            if (postList[i] != res[i]) {
                                postList[i] = res[i]
                                postAdapter.notifyItemChanged(i)
                            }
                        }else{
                                postList.add(res[i])
                                postAdapter.notifyItemInserted(i)
                        }
                    }*/
                    postList.clear()
                    postList.addAll(res)

                    postAdapter.submitList(postList)
                    //postAdapter.notifyDataSetChanged()
                    progress_circular.visibility = View.GONE
                    swipeRefreshLayout.isRefreshing = false
                    isRefreshing = false
                    mLayoutManager.scrollToPosition(0)
                } else if (response?.code() == 400 || response?.code() == 401) {
                    Log.i("getPosts", "Something went wrong")
                }
            } catch (e: Exception) {
                Log.i("getPosts", e.toString())
            }
        }
    }

    fun setClicked(postId: Int) {
        clickedPost = postId
    }

    override fun onResume() {
        if (clickedPost >= 0) {
            //postAdapter.notifyItemChanged(clickedPost,Payload.FAVORITE_CHANGE)
            //postAdapter.notifyDataSetChanged()
            //postList[clickedPost].description = postList[clickedPost].description.toString() + " "
            postAdapter.notifyItemChanged(clickedPost, COMMENT_QUANTITY_CHANGED)
            // postList[clickedPost].description = postList[clickedPost].description.toString().trim()
            clickedPost = -1
        }
        super.onResume()
    }

}