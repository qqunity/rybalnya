package com.example.rybalnya.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.rybalnya.R
import com.example.rybalnya.models.UserModel
import com.example.rybalnya.ui.chat.MessageActivity


class UserAdapter(
    private val mContext: Context, private val mUsers: List<UserModel>,
    private val ischat: Boolean
) :
    RecyclerView.Adapter<UserAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(mContext).inflate(R.layout.friend_item, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user: UserModel = mUsers[position]
        when {
            user.userName != null -> {
                holder.username.text = user.userName
            }
            user.nickName != null -> {
                holder.username.text = user.nickName
            }
            else -> {
                holder.username.text = user.eMail
            }
        }

        if (user.imageStr.equals("default") || user.imageStr == null) {
            holder.profileImage.setImageResource(R.mipmap.ic_launcher)
        } else {
            val bmp = Base64.decode(user.imageStr, 0)
            holder.profileImage.setImageBitmap(BitmapFactory.decodeByteArray(bmp, 0, bmp.size))
        }

        //show last message under name
        if (ischat) {
            holder.lastMsg.visibility = View.VISIBLE
            if (user.senderOfLastMsg != null) {
                holder.lastMsgSender.visibility = View.VISIBLE
                holder.lastMsgSender.text = user.senderOfLastMsg + ":"
            }
            holder.lastMsg.text = user.lastMsg
            if (user.isSeen == false) {
                holder.lastMsgBlock.setBackgroundResource(R.color.colorMain)
            }
        } else {
            holder.lastMsg.visibility = View.GONE
        }

        if (user.status.equals("online")) {
            holder.imgOn.visibility = View.VISIBLE
            holder.imgOff.visibility = View.GONE
        } else {
            holder.imgOn.visibility = View.GONE
            holder.imgOff.visibility = View.VISIBLE
        }
        if (ischat) {
            holder.itemView.setOnClickListener {
                val intent = Intent(mContext, MessageActivity::class.java)
                intent.putExtra("userEmail", user.eMail)
                intent.putExtra("userName", user.userName)
                mContext.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int {
        return mUsers.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var username: TextView = itemView.findViewById(R.id.username)
        var profileImage: ImageView = itemView.findViewById(R.id.profile_image)
        val imgOn: ImageView = itemView.findViewById(R.id.img_on)
        val imgOff: ImageView = itemView.findViewById(R.id.img_off)
        val lastMsg: TextView = itemView.findViewById(R.id.last_msg)
        val lastMsgSender: TextView = itemView.findViewById(R.id.last_msg_holder)
        val lastMsgBlock: ConstraintLayout = itemView.findViewById(R.id.last_msg_block)
    }


}