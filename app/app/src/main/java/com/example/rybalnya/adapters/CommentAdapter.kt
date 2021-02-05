package com.example.rybalnya.adapters

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.text.format.DateUtils
import android.util.Base64
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.rybalnya.BASE_URL
import com.example.rybalnya.MainActivity2
import com.example.rybalnya.R
import com.example.rybalnya.api.ApiRequests
import com.example.rybalnya.api.CommentJSON
import com.example.rybalnya.models.CommentModel
import com.example.rybalnya.ui.profile.AnotherUserProfileActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.awaitResponse
import retrofit2.converter.gson.GsonConverterFactory
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class CommentAdapter(context: Context, comments: ArrayList<CommentModel>) :
    RecyclerView.Adapter<CommentAdapter.ImageViewHolder>() {

    private val mContext: Context = context
    private val mComment: ArrayList<CommentModel> = comments

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view: View = LayoutInflater.from(mContext).inflate(R.layout.comment_item, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val comment: CommentModel = mComment[position]
        holder.comment.text = comment.comment
        getUserInfo(
            holder.imageProfile,
            holder.username,
            comment.publisher,
            MainActivity2().getTkn()!!
        )

        if (comment.publisher != MainActivity2().getUID()) {
            holder.imageProfile.setOnClickListener {
                val intent = Intent(mContext, AnotherUserProfileActivity::class.java)
                intent.putExtra("userId", comment.publisher)
                mContext.startActivity(intent)
            }
            holder.username.setOnClickListener {
                val intent = Intent(mContext, AnotherUserProfileActivity::class.java)
                intent.putExtra("userId", comment.publisher)
                mContext.startActivity(intent)
            }
        }

        holder.itemView.setOnLongClickListener {
            if (comment.publisher == MainActivity2().getUID()) {
                val items = arrayOf("Изменить", "Удалить")
                val simpleDialog = MaterialAlertDialogBuilder(
                    mContext,
                    R.style.ThemeOverlay_App_MaterialAlertDialog
                )
                simpleDialog.setTitle("Что вы хотите сделать?")
                simpleDialog.setItems(items) { Sdialog, which ->
                    when (which) {
                        0 -> {
                            Sdialog.dismiss()
                            val alertDialog = MaterialAlertDialogBuilder(
                                mContext,
                                R.style.ThemeOverlay_App_MaterialAlertDialog
                            )
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
                            editText.setText(holder.comment.text)
                            container.addView(editText, lp)
                            alertDialog.setView(container)

                            alertDialog.setNegativeButton("Отмена") { dialogInterface, i -> dialogInterface.cancel() }

                            alertDialog.setPositiveButton(
                                "Да"
                            ) { dialogInterface, i ->
                                val newComment = editText.text.toString()
                                holder.comment.text = newComment
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
                                        val response = api?.editComment(
                                            comment.commentId,
                                            CommentJSON(null, null, null, newComment, null, null),
                                            MainActivity2().getTkn().toString()
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

                            alertDialog.show()

                        }
                        1 -> {
                            Sdialog.dismiss()
                            val alertDialog = MaterialAlertDialogBuilder(
                                mContext,
                                R.style.ThemeOverlay_App_MaterialAlertDialog
                            )
                            alertDialog.setTitle("Хотите удалить?")
                            alertDialog.setNegativeButton("Нет") { aDialog, which2 -> aDialog.cancel() }
                            alertDialog.setPositiveButton("Да") { aDialog, which2 ->
                                val pos = mComment.indexOf(comment)
                                mComment.removeAt(pos)
                                deleteComment(
                                    comment.commentId,
                                    MainActivity2().getTkn().toString()
                                )
                                this.notifyItemRemoved(pos)
                                //this.notifyDataSetChanged()
                                aDialog.dismiss()
                            }
                            alertDialog.show()
                        }
                    }

                }
                simpleDialog.show()
            }
            true
        }

        val date: Date?
        val iso8601Format: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSX", Locale.US)
        date = iso8601Format.parse(comment.createdAt)

        val `when`: Long = date.time
        var flags = 0
        flags = flags or DateUtils.FORMAT_SHOW_TIME
        flags = flags or DateUtils.FORMAT_SHOW_DATE
        flags = flags or DateUtils.FORMAT_ABBREV_MONTH
        flags = flags or DateUtils.FORMAT_SHOW_YEAR

        holder.commentTimestamp.text = DateUtils.formatDateTime(
            mContext,
            (`when` + TimeZone.getDefault().getOffset(`when`) / 3600000.0).toLong(), flags
        )
    }

    private fun deleteComment(commentId: Int, token: String) {
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
                val response = api?.deleteComment(commentId, token)?.awaitResponse()
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
                Log.i("deleteComment", e.toString())
            }
        }
    }

    override fun getItemCount(): Int {
        return mComment.size
    }

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageProfile: ImageView = itemView.findViewById(R.id.image_profile)
        var username: TextView = itemView.findViewById(R.id.username)
        var comment: TextView = itemView.findViewById(R.id.comment)
        val commentTimestamp: TextView = itemView.findViewById(R.id.comment_time)
    }

    private fun getUserInfo(
        imageView: ImageView,
        username: TextView,
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
                    Log.i("getPosts", "Successful")
                    if (response.body()?.UserInfo?.Image != null) {
                        val bmp = Base64.decode(response.body()?.UserInfo!!.Image, 0)
                        imageView.setImageBitmap(BitmapFactory.decodeByteArray(bmp, 0, bmp.size))
                    } else {
                        imageView.setImageResource(R.mipmap.ic_launcher)
                    }
                    if (response.body()?.UserInfo!!.Nickname != "" || response.body()?.UserInfo!!.Nickname != null) {
                        username.text = response.body()!!.UserInfo!!.Nickname
                    } else if (response.body()?.UserInfo!!.FullName != "" || response.body()?.UserInfo!!.FullName != null) {
                        username.text = response.body()!!.UserInfo!!.FullName
                    } else {
                        username.text = response.body()!!.UserInfo!!.Email
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