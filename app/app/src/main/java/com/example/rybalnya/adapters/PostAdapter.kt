package com.example.rybalnya.adapters

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Handler
import android.os.Looper
import android.text.format.DateUtils
import android.util.Base64
import android.util.Log
import android.view.*
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.*
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.PopupMenu
import androidx.asynclayoutinflater.view.AsyncLayoutInflater
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.rybalnya.BASE_URL
import com.example.rybalnya.COMMENT_QUANTITY_CHANGED
import com.example.rybalnya.MainActivity2
import com.example.rybalnya.R
import com.example.rybalnya.api.ApiRequests
import com.example.rybalnya.api.PostRecieve
import com.example.rybalnya.api.ResponseJSON
import com.example.rybalnya.models.PostModel
import com.example.rybalnya.ui.home.CommentsActivity
import com.example.rybalnya.ui.home.HomeFragment
import com.example.rybalnya.ui.profile.AnotherUserProfileActivity
import com.example.rybalnya.utils.ExpandableTextView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.awaitResponse
import retrofit2.converter.gson.GsonConverterFactory
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class PostAdapter(
    private val mContext: Context,
    private val mPosts: ArrayList<PostModel>
) : ListAdapter<PostModel, PostAdapter.ImageViewHolder>(MyDiffCallback()) {

    companion object {
        const val NUM_CACHED_VIEWS = 15
    }

    private val asyncLayoutInflater = AsyncLayoutInflater(mContext)
    private val cachedViews = Stack<View>()

    init {
        //Create some views asynchronously and add them to our stack
        for (i in 0..NUM_CACHED_VIEWS) {
            asyncLayoutInflater.inflate(R.layout.post_item, null) { view, layoutRes, viewGroup ->
                cachedViews.push(view)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostAdapter.ImageViewHolder {

        val view: View = if (cachedViews.isEmpty()) {
            LayoutInflater.from(mContext).inflate(R.layout.post_item, parent, false)
        } else {
            cachedViews.pop()
                .also { it.layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT) }
        }
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: PostAdapter.ImageViewHolder,
        position: Int,
        payloads: List<Any>
    ) {
        if (payloads.isNotEmpty()) {
            if (payloads[0] == COMMENT_QUANTITY_CHANGED) {
                nrComments(
                    holder.comments,
                    mPosts[position].postId,
                    MainActivity2().getTkn().toString()
                )
            }
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    override fun onBindViewHolder(holder: PostAdapter.ImageViewHolder, position: Int) {

        val post: PostModel = mPosts[position]


        UIJobScheduler.submitJob {
            if (post.description.equals("")) {
                holder.description.visibility = View.GONE
            } else {
                holder.description.visibility = View.VISIBLE
                holder.description.text = post.description
            }
        }

        /*val bmp = Base64.decode(post.postImage, 0)
        holder.postImage.setImageBitmap(BitmapFactory.decodeByteArray(bmp, 0, bmp.size))*/
        UIJobScheduler.submitJob {
            SetImageBitmap(post, holder.postImage).execute()
        }

        holder.like.setOnClickListener {
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
                    val response: Response<ResponseJSON>? = if (holder.like.tag == "like") {
                        api?.addLike(post.postId, MainActivity2().getTkn().toString())
                            ?.awaitResponse()
                    } else {
                        api?.deleteLike(post.postId, MainActivity2().getTkn().toString())
                            ?.awaitResponse()
                    }
                    Log.i("Response", response.toString())
                    Log.i("Response body", response?.body().toString())
                    if (response?.code() == 201) {
                        Toast.makeText(
                            mContext,
                            response.body()?.Action.toString(),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else if (response?.code() == 400 || response?.code() == 401) {
                        Toast.makeText(
                            mContext,
                            response.body()?.Error.toString(),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    isLiked(post.postId, holder.like, MainActivity2().getTkn().toString())
                    nrLikes(holder.likes, post.postId, MainActivity2().getTkn().toString())
                } catch (e: Exception) {
                    Log.i("like.setOnClickListener", e.toString())
                }
            }
        }

        UIJobScheduler.submitJob {
            publisherInfo(
                holder.imageProfile,
                holder.username,
                holder.publisher,
                post.publisher,
                MainActivity2().getTkn()!!
            )
        }

        UIJobScheduler.submitJob {
            isLiked(post.postId, holder.like, MainActivity2().getTkn().toString())
            nrLikes(holder.likes, post.postId, MainActivity2().getTkn().toString())
            nrComments(holder.comments, post.postId, MainActivity2().getTkn().toString())
        }

        val date: Date?
        val iso8601Format: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSX", Locale.US)
        date = iso8601Format.parse(post.createdAt)

        val `when`: Long = date.time
        var flags = 0
        flags = flags or DateUtils.FORMAT_SHOW_TIME
        flags = flags or DateUtils.FORMAT_SHOW_DATE
        flags = flags or DateUtils.FORMAT_ABBREV_MONTH
        flags = flags or DateUtils.FORMAT_SHOW_YEAR

        UIJobScheduler.submitJob {
            holder.timeStm.text = DateUtils.formatDateTime(
                mContext,
                (`when` + TimeZone.getDefault().getOffset(`when`) / 3600000.0).toLong(), flags
            )
            holder.timeStm.visibility = View.VISIBLE

            holder.comment.setOnClickListener {
                HomeFragment().setClicked(position)
                Log.i("Setting id", post.postId.toString())
                val intent = Intent(mContext, CommentsActivity::class.java)
                intent.putExtra("postid", post.postId)
                intent.putExtra("publisherid", post.publisher)
                mContext.startActivity(intent)
            }
            holder.comments.setOnClickListener {
                HomeFragment().setClicked(position)
                Log.i("Setting id", post.postId.toString())
                val intent = Intent(mContext, CommentsActivity::class.java)
                intent.putExtra("postid", post.postId)
                intent.putExtra("publisherid", post.publisher)
                mContext.startActivity(intent)
            }

/*      TODO("Create likedBy activity")
        holder.likes.setOnClickListener {
            val intent = Intent(mContext, LikedByActivity::class.java)
            intent.putExtra("id", post.postId)
            intent.putExtra("title", "likes")
            mContext.startActivity(intent)
        }*/
        }

        UIJobScheduler.submitJob {
            if (post.publisher != MainActivity2().getUID()) {
                holder.imageProfile.setOnClickListener {
                    val intent = Intent(mContext, AnotherUserProfileActivity::class.java)
                    intent.putExtra("userId", post.publisher)
                    mContext.startActivity(intent)
                }
                holder.username.setOnClickListener {
                    val intent = Intent(mContext, AnotherUserProfileActivity::class.java)
                    intent.putExtra("userId", post.publisher)
                    mContext.startActivity(intent)
                }
                holder.more.visibility = View.GONE
            } else {
                holder.imageProfile.setOnClickListener {}
                holder.username.setOnClickListener {}
                holder.more.visibility = View.VISIBLE
                holder.more.setOnClickListener { view ->
                    val wrapper = ContextThemeWrapper(mContext, R.style.BasePopupMenu)
                    val popupMenu = PopupMenu(wrapper, view)
                    popupMenu.inflate(R.menu.post_menu)
                    popupMenu.setOnMenuItemClickListener { menuItem ->
                        when (menuItem.itemId) {
                            R.id.edit -> {
                                editPost(
                                    post.postId,
                                    holder.description,
                                    MainActivity2().getTkn().toString()
                                )
                                true
                            }
                            R.id.delete -> {
                                deletePost(post, MainActivity2().getTkn().toString())
                                true
                            }
/*                    R.id.report -> {
                        Toast.makeText(
                            mContext,
                            "Reported clicked!",
                            Toast.LENGTH_SHORT
                        ).show()
                        true
                    }*/
                            else -> false
                        }
                    }
                    popupMenu.show()
                }
            }
        }
    }

    override fun getItemId(position: Int): Long {
        return mPosts[position].postId.hashCode().toLong()
    }

    class MyDiffCallback : DiffUtil.ItemCallback<PostModel>() {
        override fun areItemsTheSame(oldItem: PostModel, newItem: PostModel): Boolean {
            return oldItem.postId == newItem.postId
        }

        override fun areContentsTheSame(oldItem: PostModel, newItem: PostModel): Boolean {
            return oldItem == newItem
        }

    }

    object UIJobScheduler {
        private const val MAX_JOB_TIME_MS: Float = 4f

        private var elapsed = 0L
        private val jobQueue = ArrayDeque<() -> Unit>()
        private val isOverMaxTime get() = elapsed > MAX_JOB_TIME_MS * 1_000_000
        private val handler = Handler(Looper.getMainLooper())

        fun submitJob(job: () -> Unit) {
            jobQueue.add(job)
            if (jobQueue.size == 1) {
                handler.post { processJobs() }
            }
        }

        private fun processJobs() {
            while (!jobQueue.isEmpty() && !isOverMaxTime) {
                val start = System.nanoTime()
                jobQueue.poll().invoke()
                elapsed += System.nanoTime() - start
            }
            if (jobQueue.isEmpty()) {
                elapsed = 0
            } else if (isOverMaxTime) {
                onNextFrame {
                    elapsed = 0
                    processJobs()
                }
            }
        }

        private fun onNextFrame(callback: () -> Unit) =
            Choreographer.getInstance().postFrameCallback { callback() }
    }

    private fun editPost(postId: Int, desc: TextView, token: String) {
        val alertDialog =
            MaterialAlertDialogBuilder(mContext, R.style.ThemeOverlay_App_MaterialAlertDialog)
        alertDialog.setTitle("Изменить?")

        val editText = EditText(mContext)
        val container = LinearLayout(mContext)
        container.orientation = LinearLayout.VERTICAL
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        lp.setMargins(50, 0, 50, 0)
        editText.layoutParams = lp
        editText.gravity = Gravity.TOP or Gravity.START
        editText.setText(desc.text.toString())
        container.addView(editText, lp)
        alertDialog.setView(container)

        alertDialog.setPositiveButton(
            "Да"
        ) { dialogInterface, i ->
            val newDescription = editText.text.toString()
            desc.text = newDescription
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
                    val response = api?.editPostDescription(
                        postId,
                        PostRecieve(newDescription, null, null, null, null),
                        token
                    )?.awaitResponse()
                    Log.i("Response", response.toString())
                    Log.i("Response body", response?.body().toString())
                    if (response?.code() == 200) {
                        Toast.makeText(
                            mContext,
                            response.body()?.Action.toString(),
                            Toast.LENGTH_SHORT
                        ).show()

                    } else if (response?.code() == 400 || response?.code() == 401) {
                        Toast.makeText(
                            mContext,
                            response.body()?.Error.toString(),
                            Toast.LENGTH_SHORT
                        ).show()

                    }
                } catch (e: Exception) {
                    Log.i("deletePost", e.toString())
                }
            }

            dialogInterface.dismiss()
        }
        alertDialog.setNegativeButton(
            "Отмена"
        ) { dialogInterface, i -> dialogInterface.cancel() }
        alertDialog.show()
    }

    private fun deletePost(post: PostModel, token: String) {

        val alertDialog =
            MaterialAlertDialogBuilder(mContext, R.style.ThemeOverlay_App_MaterialAlertDialog)
        alertDialog.setTitle("Вы уверены?")

        alertDialog.setPositiveButton(
            "Да"
        ) { dialogInterface, i ->
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
                    val response = api?.deletePost(post.postId, token)?.awaitResponse()
                    Log.i("Response", response.toString())
                    Log.i("Response body", response?.body().toString())
                    if (response?.code() == 200) {
                        Toast.makeText(
                            mContext,
                            response.body()?.Action.toString(),
                            Toast.LENGTH_SHORT
                        ).show()

                    } else if (response?.code() == 400 || response?.code() == 401) {
                        Toast.makeText(
                            mContext,
                            response.body()?.Error.toString(),
                            Toast.LENGTH_SHORT
                        ).show()

                    }
                } catch (e: Exception) {
                    Log.i("deletePost", e.toString())
                }
            }
            val pos = mPosts.indexOf(post)
            mPosts.removeAt(pos)
            this.notifyItemRemoved(pos)
        }
        alertDialog.setNegativeButton(
            "Отмена"
        ) { dialogInterface, i -> dialogInterface.cancel() }
        alertDialog.show()

    }

    override fun getItemCount(): Int {
        return mPosts.size
    }

    private fun nrComments(comments: TextView, postId: Int, token: String) {
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
                val response = api?.countComments(postId, token)?.awaitResponse()
                Log.i("Response", response.toString())
                Log.i("Response body", response?.body().toString())
                if (response?.code() == 200) {
                    response.body()?.CntComments?.let { comments.text = it.toString() }
                } else if (response?.code() == 400 || response?.code() == 401) {
                    comments.text = "0"
                }
            } catch (e: Exception) {
                Log.i("nrLikes", e.toString())
            }
        }
    }

    private fun nrLikes(likes: TextView, postId: Int, token: String) {
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
                val response = api?.countLikes(postId, token)?.awaitResponse()
                Log.i("Response", response.toString())
                Log.i("Response body", response?.body().toString())
                if (response?.code() == 200) {
                    response.body()?.CntLikes?.let { likes.text = it.toString() }
                } else if (response?.code() == 400 || response?.code() == 401) {
                    likes.text = "0"
                }
            } catch (e: Exception) {
                Log.i("nrLikes", e.toString())
            }
        }
    }

    private fun isLiked(postId: Int, likeView: ImageView, token: String) {
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
                val response = api?.isLiked(postId, token)?.awaitResponse()
                Log.i("Response", response.toString())
                Log.i("Response body", response?.body().toString())
                if (response?.body()?.IsLiked == true) {
                    likeView.setImageResource(R.drawable.ic_liked)
                    likeView.tag = "liked"
                } else {
                    likeView.setImageResource(R.drawable.ic_like)
                    likeView.tag = "like"
                }
            } catch (e: Exception) {
                Log.i("isLiked", e.toString())
            }
        }
    }

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageProfile: ImageView = itemView.findViewById(R.id.image_profile)
        var postImage: ImageView = itemView.findViewById(R.id.post_image)
        var like: ImageView = itemView.findViewById(R.id.like)
        var comment: ImageView = itemView.findViewById(R.id.comment)
        var more: ImageView = itemView.findViewById(R.id.more)
        var username: TextView = itemView.findViewById(R.id.username)
        var likes: TextView = itemView.findViewById(R.id.likes)
        var publisher: TextView = itemView.findViewById(R.id.publisher)
        var description: ExpandableTextView = itemView.findViewById(R.id.description)
        var comments: TextView = itemView.findViewById(R.id.comments)
        var timeStm: TextView = itemView.findViewById(R.id.time_stamp)
    }

    private fun publisherInfo(
        imageProfile: ImageView,
        username: TextView,
        publisher: TextView,
        publisherid: Int,
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
                val response = api?.getUserById(publisherid, token)?.awaitResponse()
                Log.i("Response", response.toString())
                Log.i("Response body", response?.body().toString())
                if (response?.code() == 200) {
                    Log.i("getPublisherInfo", "Successful")
                    if (response.body()?.UserInfo?.Image != null) {
                        val bmp = Base64.decode(response.body()?.UserInfo!!.Image, 0)
                        imageProfile.setImageBitmap(BitmapFactory.decodeByteArray(bmp, 0, bmp.size))
                    } else {
                        imageProfile.setImageResource(R.mipmap.ic_launcher)
                    }
                    if (response.body()?.UserInfo!!.Nickname != "" || response.body()?.UserInfo!!.Nickname != null) {
                        username.text = response.body()!!.UserInfo!!.Nickname
//                        publisher.text = response.body()!!.UserInfo!!.Nickname
                    } else if (response.body()?.UserInfo!!.FullName != "" || response.body()?.UserInfo!!.FullName != null) {
                        username.text = response.body()!!.UserInfo!!.FullName
//                        publisher.text = response.body()!!.UserInfo!!.FullName
                    } else {
                        username.text = response.body()!!.UserInfo!!.Email
//                        publisher.text = response.body()!!.UserInfo!!.Email
                    }
                } else if (response?.code() == 400 || response?.code() == 401) {
                    Log.i("getUserInfo", "Something went wrong")
                    Toast.makeText(
                        mContext,
                        response.body()?.Error.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.i("Упс...", e.toString())
            }
        }
    }
}

class SetImageBitmap(private var post: PostModel, private var iv: ImageView) :
    AsyncTask<Void, Void, Bitmap>() {
    override fun doInBackground(vararg params: Void): Bitmap {
        val bmpArr = Base64.decode(post.postImage, 0)
        return BitmapFactory.decodeByteArray(bmpArr, 0, bmpArr.size)
    }

    override fun onPostExecute(bitmap: Bitmap) {
        iv.setImageBitmap(bitmap)
        iv.visibility = View.VISIBLE
    }
}
