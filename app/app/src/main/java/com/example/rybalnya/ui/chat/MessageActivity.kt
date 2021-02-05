package com.example.rybalnya.ui.chat

import android.os.Bundle
import android.text.format.DateUtils
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rybalnya.BASE_URL
import com.example.rybalnya.MainActivity2
import com.example.rybalnya.R
import com.example.rybalnya.adapters.MessageAdapter
import com.example.rybalnya.api.ApiRequests
import com.example.rybalnya.api.MessageReceive
import com.example.rybalnya.models.MessageModel
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.activity_message.*
import kotlinx.coroutines.*
import retrofit2.Retrofit
import retrofit2.awaitResponse
import retrofit2.converter.gson.GsonConverterFactory
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class MessageActivity : AppCompatActivity() {

    private lateinit var mSocket: Socket
    private lateinit var recyclerView: RecyclerView
    private lateinit var usrEmail: String
    private lateinit var roomName: String
    private var notify = false
    private lateinit var msgAdapter: MessageAdapter
    private var mMessages: ArrayList<MessageModel> = ArrayList()
    private lateinit var myUserEmail: String
    private lateinit var magsReceive: ArrayList<MessageReceive>


    override fun onCreate(savedInstanceState: Bundle?) = runBlocking {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)

        topAppBar.title = getFriendName()
        topAppBar.setNavigationOnClickListener {
            mSocket.disconnect()
            onBackPressed()
        }

        usrEmail = intent.getStringExtra("userEmail").toString()
        myUserEmail = MainActivity2().getUserEmail()
        roomName = setRoomName(usrEmail, myUserEmail)


        recyclerView = findViewById(R.id.recycler_view_messages)
        recyclerView.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(applicationContext)
        linearLayoutManager.stackFromEnd = true
        recyclerView.layoutManager = linearLayoutManager
        var userImg = async { getUserImage(usrEmail) }
        msgAdapter = MessageAdapter(
            this@MessageActivity, mMessages, imageStr = userImg.await(),
            EMAIL = usrEmail
        )
        recyclerView.adapter = msgAdapter

        readMessages()

        establishConnection()
        setSocketListeners()



        btn_send.setOnClickListener {
            notify = true
            val msg = text_send.text.toString()
            if (msg != "") {
                sendMessage(msg)
            } else {
                Toast.makeText(
                    this@MessageActivity,
                    "Зачем вы отправляете пустое сообщение?",
                    Toast.LENGTH_SHORT
                ).show()
            }
            text_send.setText("")
        }

        seenMessage()
    }

    private fun getUserImage(user: String): String {

        var imageStr = "default"

        GlobalScope.launch {
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
            try {
                val response = api?.getUserInfoByEmail(user, MainActivity2().getTkn().toString())
                    ?.awaitResponse()

                Log.i("Response", response.toString())
                Log.i("Response body", response?.body().toString())
                if (response?.code() == 200) {
                    Log.i("getUser", "Successful")
                    imageStr = response.body()?.UserInfo!!.Image.toString()
                } else if (response?.code() == 400 || response?.code() == 401) {
                    Log.i("getUser", "Something went wrong")
                    Toast.makeText(
                        this@MessageActivity,
                        response.body()?.Error.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.i("getUser", e.toString())
            }
        }
        return imageStr
    }

    private fun setSocketListeners() {
        var myUserID = MainActivity2().getUID()
        mSocket.on("newMsg") { params ->
            var isSeen: Boolean
            isSeen = params[1].toString() == "true"
            if (params[1].toString() != myUserEmail) {
                mSocket.emit("msgRecive", roomName, myUserEmail)
                runOnUiThread {
                    mMessages.add(
                        MessageModel(
                            "you",
                            usrEmail,
                            params[0].toString(),
                            true,
                            formatDate(params[2].toString(), "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSSX")
                        )
                    )
                    msgAdapter.notifyDataSetChanged()
                    recyclerView.scrollToPosition(mMessages.size - 1)
                }
            } else {
                runOnUiThread {
                    mMessages.add(
                        MessageModel(
                            usrEmail,
                            "you",
                            params[0].toString(),
                            isSeen,
                            formatDate(params[2].toString(), "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSSX")
                        )
                    )
                    msgAdapter.notifyDataSetChanged()
                    recyclerView.scrollToPosition(mMessages.size - 1)
                }
            }
        }
        mSocket.on("newCheck") { params ->
            if (params[0].toString() != myUserEmail) {
                mMessages.last().isSeen = true
                runOnUiThread {
                    msgAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    private fun establishConnection() {
        try {
            var params = IO.Options()
            params.reconnectionDelay = 1
            mSocket = IO.socket("http://37.230.114.186:8080", params)
            mSocket.on(Socket.EVENT_CONNECT, Emitter.Listener {
                Log.d("connect", "Success")
                mSocket.emit("join", roomName, myUserEmail)
            })
            mSocket.connect()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("fail", "Failed to connect")
        }
    }

    private fun setRoomName(mail1: String, mail2: String): String {
        return if (mail1 < mail2) {
            "$mail1|$mail2"
        } else {
            "$mail2|$mail1"
        }
    }

    override fun onDestroy() {
        mSocket.emit("bye")
        mSocket.off("newMsg")
        mSocket.disconnect()
        super.onDestroy()
    }

    override fun onPause() {
        mSocket.emit("bye")
        mSocket.off("newMsg")
        mSocket.disconnect()
        super.onPause()
    }


    private fun getFriendName(): String {
        return intent.getStringExtra("userName").toString()
    }

    private fun seenMessage() {
//        TODO("When someone opens the dialog, all the messages are marked as seen(send changes to DB)")

    }

    private fun sendMessage(
        msg: String
    ) {
        mSocket.emit("msg", msg, setRoomName(usrEmail, myUserEmail), myUserEmail, false)
        recyclerView.adapter = msgAdapter
    }

    private fun readMessages() {
        mMessages.clear()
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
                    api?.getMsgs(roomName, MainActivity2().getTkn().toString())?.awaitResponse()

                Log.i("Response", response.toString())
                Log.i("Response body", response?.body().toString())
                if (response?.code() == 200) {
                    Log.i("getMsgs", "Successful")
                    Toast.makeText(
                        this@MessageActivity,
                        response.body()?.Action.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                    if (response.body()?.AllMsgs != null) {
                        magsReceive = ArrayList()
                        magsReceive = response.body()!!.AllMsgs!!
                    } else {
                        Toast.makeText(
                            this@MessageActivity,
                            "Сообщения не найдены",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    for (msg in magsReceive) {
                        if (msg.UserID != MainActivity2().getUID()) {
                            runOnUiThread {
                                mMessages.add(
                                    MessageModel(
                                        "you",
                                        usrEmail,
                                        msg.Message,
                                        true,
                                        formatDate(msg.CreatedAt!!, "yyyy-MM-dd'T'HH:mm:ss.SSSSSSX")
                                    )
                                )
                                msgAdapter.notifyDataSetChanged()
                                recyclerView.scrollToPosition(mMessages.size - 1)
                            }
                        } else {
                            runOnUiThread {
                                mMessages.add(
                                    MessageModel(
                                        usrEmail,
                                        "you",
                                        msg.Message,
                                        msg.IsSeen,
                                        formatDate(msg.CreatedAt!!, "yyyy-MM-dd'T'HH:mm:ss.SSSSSSX")
                                    )
                                )
                                msgAdapter.notifyDataSetChanged()
                                recyclerView.scrollToPosition(mMessages.size - 1)
                            }
                        }

                    }

                    recyclerView.adapter = msgAdapter

                } else if (response?.code() == 400 || response?.code() == 401) {
                    Log.i("getMsgs", "Something went wrong")
                    Toast.makeText(
                        this@MessageActivity,
                        response.body()?.Error.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.i("getMsgs", e.toString())
            }
        }
    }

    private fun formatDate(toParse: String, format: String): String {
        val date: Date?
        val iso8601Format: DateFormat = SimpleDateFormat(format, Locale.US)
        date = iso8601Format.parse(toParse)

        val `when`: Long = date.time
        var flags = 0
        flags = flags or DateUtils.FORMAT_SHOW_TIME
        flags = flags or DateUtils.FORMAT_SHOW_DATE
        flags = flags or DateUtils.FORMAT_NUMERIC_DATE

        return DateUtils.formatDateTime(
            this,
            (`when` + TimeZone.getDefault().getOffset(`when`) / 3600000.0).toLong(), flags
        )
    }

}