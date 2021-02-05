package com.example.rybalnya.adapters

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.rybalnya.R
import com.example.rybalnya.models.MessageModel


class MessageAdapter(
    private val mContext: Context, private val mMessage: List<MessageModel>,
    private val imageStr: String, private val EMAIL: String
) :
    RecyclerView.Adapter<MessageAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return if (viewType == MSG_TYPE_RIGHT) {
            val view: View =
                LayoutInflater.from(mContext).inflate(R.layout.msg_item_right, parent, false)
            ViewHolder(view)
        } else {
            val view: View =
                LayoutInflater.from(mContext).inflate(R.layout.msg_item_left, parent, false)
            ViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message: MessageModel = mMessage[position]
        holder.showMessage.text = message.message
        if (imageStr == "default") {
            holder.profileImage.setImageResource(R.mipmap.ic_launcher)
        } else {
            val bmp = Base64.decode(imageStr, 0)
            holder.profileImage.setImageBitmap(BitmapFactory.decodeByteArray(bmp, 0, bmp.size))
        }
        holder.createdAt.text = message.createdAt
        if (position == mMessage.size - 1) {
            if (message.isSeen!!) {
                holder.txtSeen.visibility = View.VISIBLE
                holder.txtSeen.text = "✓✓"
            } else {
                holder.txtSeen.visibility = View.VISIBLE
                holder.txtSeen.text = "✓"
            }
        } else {
            holder.txtSeen.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return mMessage.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var showMessage: TextView = itemView.findViewById(R.id.show_message)
        var profileImage: ImageView = itemView.findViewById(R.id.profile_image)
        var txtSeen: TextView = itemView.findViewById(R.id.txt_seen)
        var createdAt: TextView = itemView.findViewById(R.id.msg_timestamp)
    }

    override fun getItemViewType(position: Int): Int {
//      TODO("get sender of message to define type of the msg(where to show: left or right side)")
        return if (mMessage[position].sender?.equals(EMAIL)!!) {
            MSG_TYPE_RIGHT
        } else {
            MSG_TYPE_LEFT
        }
    }

    companion object {
        const val MSG_TYPE_LEFT = 0
        const val MSG_TYPE_RIGHT = 1
    }

}