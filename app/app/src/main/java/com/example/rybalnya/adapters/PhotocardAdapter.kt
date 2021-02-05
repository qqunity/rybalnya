package com.example.rybalnya.adapters

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.rybalnya.R
import com.example.rybalnya.models.PostModel


class PhotocardAdapter(context: Context, posts: ArrayList<PostModel>) :
    RecyclerView.Adapter<PhotocardAdapter.ImageViewHolder>() {
    private val mContext: Context = context
    private val mPosts: ArrayList<PostModel> = posts
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view: View =
            LayoutInflater.from(mContext).inflate(R.layout.photocard_item, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val post: PostModel = mPosts[position]
        val bmp = Base64.decode(post.postImage, 0)
        holder.postImage.setImageBitmap(BitmapFactory.decodeByteArray(bmp, 0, bmp.size))
        holder.itemView.setPadding(1, 1, 1, 1)
/*        holder.post_image.setOnClickListener(object : OnClickListener() {
            fun onClick(view: View?) {
                val editor: SharedPreferences.Editor =
                    mContext.getSharedPreferences("PREFS", MODE_PRIVATE).edit()
                editor.putString("postid", post.getPostid())
                editor.apply()
                (mContext as FragmentActivity).supportFragmentManager.beginTransaction().replace(
                    R.id.fragment_container,
                    PostDetailFragment()
                ).commit()
            }
        })*/
    }

    override fun getItemCount(): Int {
        return mPosts.size
    }

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var postImage: ImageView = itemView.findViewById(R.id.post_image)
    }

}