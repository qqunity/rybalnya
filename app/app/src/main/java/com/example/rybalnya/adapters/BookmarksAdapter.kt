package com.example.rybalnya.adapters

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.rybalnya.R
import com.example.rybalnya.models.BookmarkModel
import com.example.rybalnya.utils.ExpandableTextView

class BookmarksAdapter(
    private val mContext: Context,
    private val mBookmarks: ArrayList<BookmarkModel>
) :
    RecyclerView.Adapter<BookmarksAdapter.BaseViewHolder<*>>() {

    companion object {
        private const val TYPE_PLACEMARK = 0
        private const val TYPE_POST = 1
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        return when (viewType) {
            TYPE_PLACEMARK -> {
                val view: View =
                    LayoutInflater.from(mContext)
                        .inflate(R.layout.bookmark_place_item, parent, false)
                PlacemarkViewHolder(view)
            }
            TYPE_POST -> {
                val view: View =
                    LayoutInflater.from(mContext)
                        .inflate(R.layout.post_item, parent, false)
                PostViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }


    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        val bookmark = mBookmarks[position]
        when (holder) {
            is PlacemarkViewHolder -> holder.bind(bookmark)
            is PostViewHolder -> holder.bind(bookmark)
            else -> throw IllegalArgumentException()
        }
    }

    override fun getItemCount(): Int {
        return mBookmarks.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (TextUtils.isEmpty(mBookmarks[position].postImage)) {
            TYPE_PLACEMARK
        } else {
            TYPE_POST
        }
        /*
    val comparable = data[position]
    return when (comparable) {
        is String -> TYPE_FAMILY
        is Trailer -> TYPE_FRIEND
        is Review -> TYPE_COLLEAGUE
        else -> throw IllegalArgumentException("Invalid type of data " + position)
    }
    */
    }

    abstract class BaseViewHolder<T>(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(item: T)
    }

    inner class PlacemarkViewHolder(itemView: View) : BaseViewHolder<BookmarkModel>(itemView) {
        var latitude: TextView = itemView.findViewById(R.id.lat)
        var longitude: TextView = itemView.findViewById(R.id.lon)
        var copyButton: ImageButton = itemView.findViewById(R.id.button_copy)
        var placeName: TextView = itemView.findViewById(R.id.place_name)
        var placeDescription: TextView = itemView.findViewById(R.id.place_description)

        override fun bind(item: BookmarkModel) {
            latitude.text = item.latitude!!.toString()
            longitude.text = item.longitude!!.toString()
            copyButton.setOnClickListener {
                val clipboardManager =
                    mContext.getSystemService(AppCompatActivity.CLIPBOARD_SERVICE) as ClipboardManager
                val clipData = ClipData.newPlainText(
                    "Coordinates",
                    (item.latitude!!.toString() + ", " + item.longitude!!.toString())
                )
                clipboardManager.setPrimaryClip(clipData)
            }
            if (item.name != null && item.name != "") {
                placeName.text = item.name.toString()
            }
            if (item.description != null && item.description != "") {
                placeDescription.text = item.description.toString()
            }
        }
    }

    inner class PostViewHolder(itemView: View) : BaseViewHolder<BookmarkModel>(itemView) {
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

        override fun bind(item: BookmarkModel) {
        }
    }

}