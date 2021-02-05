package com.example.rybalnya.ui.chat

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rybalnya.BASE_URL
import com.example.rybalnya.MainActivity2
import com.example.rybalnya.R
import com.example.rybalnya.adapters.UserAdapter
import com.example.rybalnya.api.ApiRequests
import com.example.rybalnya.api.MessageReceive
import com.example.rybalnya.api.UserRecieve
import com.example.rybalnya.models.UserModel
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.awaitResponse
import retrofit2.converter.gson.GsonConverterFactory


//private const val ARG_OBJECT = "object"

class TalksFragment : Fragment() {

    private lateinit var USER_TKN: String
    private lateinit var recyclerView: RecyclerView
    private lateinit var usersList: ArrayList<UserModel>
    private lateinit var userRecive: ArrayList<UserRecieve>
    private lateinit var onlineRecive: ArrayList<Boolean>
    private lateinit var lastMsgsRecieve: ArrayList<MessageReceive>
    private lateinit var mOnlineSocket: Socket

    //    private lateinit var mUsers: ArrayList<UserModel>
    private lateinit var userAdapter: UserAdapter
    // private var usersList: ArrayList<Chatlist>? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_talks, container, false)

        recyclerView = view.findViewById(R.id.recycler_view_talks)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)

        USER_TKN = MainActivity2().getTkn().toString()
        usersList = ArrayList()
        userAdapter = UserAdapter(requireContext(), usersList, true)
        getUsers(USER_TKN)
        connectOnlineSocket(MainActivity2().getUserEmail())
        return view
    }

    private fun connectOnlineSocket(userEmail: String) {
        try {
            var params = IO.Options()
            params.reconnectionDelay = 1
            mOnlineSocket = IO.socket("http://37.230.114.186:7777", params)
            mOnlineSocket.on(io.socket.client.Socket.EVENT_CONNECT, Emitter.Listener {
                Log.d("connect", "Success")
                //mOnlineSocket.emit("joinTalks", userEmail)
            })
            mOnlineSocket.connect()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("fail", "Failed to connect")
        }
        mOnlineSocket.on("newOnline") { params ->
            GlobalScope.launch(Dispatchers.Main) {
                if (params[0].toString() != userEmail) {
                    for (user in usersList) {
                        if (user.eMail == params[0].toString()) {
                            user.status = "online"
                            userAdapter.notifyDataSetChanged()
                            break
                        }
                    }
                }
            }
        }
        mOnlineSocket.on("newOffline") { params ->

            if (params[0].toString() != userEmail) {

                GlobalScope.launch(Dispatchers.Main) {
                    for (user in usersList) {
                        if (user.eMail == params[0].toString()) {
                            user.status = "offline"
                            userAdapter.notifyDataSetChanged()
                            break
                        }
                    }
                }

            }
        }
    }

    override fun onDestroy() {
        mOnlineSocket.disconnect()
        super.onDestroy()
    }

    private fun getUsers(token: String) {
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
                val response = api?.getUsersWithLastMsgs(MainActivity2().getUserEmail(), token)
                    ?.awaitResponse()

                Log.i("Response", response.toString())
                Log.i("Response body", response?.body().toString())
                if (response?.code() == 200) {
                    Log.i("getUsers", "Successful")

                    if (response.body()?.Users != null) {
                        userRecive = ArrayList()
                        onlineRecive = ArrayList()
                        lastMsgsRecieve = ArrayList()
                        userRecive = response.body()!!.Users!!
                        onlineRecive = response.body()!!.Online!!
                        lastMsgsRecieve = response.body()!!.LastMsgs
                    } else {
                        Toast.makeText(
                            this@TalksFragment.requireActivity(),
                            "Диалоги не найдены",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    var i = 0
                    var myID = MainActivity2().getUID()
                    for (user in userRecive) {
                        if (user.FullName == "") {
                            user.FullName = user.Email
                        }
                        var online = "online"
                        if (!onlineRecive[i]) {
                            online = "offline"
                        }
                        var sender: String? = null
                        if (lastMsgsRecieve[i].UserID == myID) {
                            sender = "Вы"
                        }
                        usersList.add(
                            UserModel(
                                user.FullName,
                                user.Nickname,
                                user.Email,
                                user.Image,
                                online,
                                lastMsgsRecieve[i].Message,
                                sender,
                                lastMsgsRecieve[i].IsSeen
                            )
                        )
                        i++
                    }

                    recyclerView.adapter = userAdapter

                } else if (response?.code() == 400 || response?.code() == 401) {
                    Log.i("getAllUsersInfo", "Something went wrong")
                    Toast.makeText(
                        this@TalksFragment.requireActivity(),
                        response.body()?.Error.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.i("getAllUsersInfo", e.toString())
            }
        }
    }

}