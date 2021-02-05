package com.example.rybalnya

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.asLiveData
import androidx.lifecycle.observe
import androidx.navigation.NavController
import com.example.rybalnya.api.ApiRequests
import com.example.rybalnya.ui.chat.ChatFragment
import com.example.rybalnya.ui.home.HomeFragment
import com.example.rybalnya.ui.map.MapFragment
import com.example.rybalnya.ui.newPost.PublishActivity
import com.example.rybalnya.ui.profile.ProfileFragment
import com.example.rybalnya.utils.UserManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main2.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.awaitResponse
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.system.exitProcess

private var USER_EMAIL = ""
private var USER_FullName = ""
private var USER_TKN = ""
private var USER_Id = 0
private var idAndTknSet = false
const val COMMENT_QUANTITY_CHANGED = 100
const val FORECAST_EXPANDED = 200
const val FORECAST_CLOSED = 201

class MainActivity2 : AppCompatActivity() {

    private val home = HomeFragment()
    private val chat = ChatFragment()
    private val profile = ProfileFragment()
    private val map = MapFragment()

    val fm: FragmentManager = supportFragmentManager
    var active: Fragment = home

    private lateinit var nick: String
    private lateinit var fullName: String
    private lateinit var bio: String
    private lateinit var mOnlineSocket: Socket

    private val userManager = UserManager(this)

    private var backWasPressedOnLogin = false

    private fun badgeSetup(id: Int, alerts: Int) {
        val badge = nav_view.getOrCreateBadge(id)
        badge.isVisible = true
        badge.number = alerts
    }

    private fun badgeClear(id: Int) {
        val badge = nav_view.getBadge(id)
        if (badge != null) {
            badge.isVisible = false
            badge.clearNumber()
        }
    }

    private inline fun startCoroutineTimer(
        delayMillis: Long = 2000,
        crossinline action: () -> Unit
    ) = GlobalScope.launch(Dispatchers.Unconfined) {
        delay(delayMillis)
        Log.i("timer", "backWasPressedOnLogin = false")
        action()
    }

    override fun onBackPressed() {
        if (!backWasPressedOnLogin) {
            backWasPressedOnLogin = true
            startCoroutineTimer { backWasPressedOnLogin = false }
            val toast = Toast.makeText(this, "Нажмите еще раз для выхода", Toast.LENGTH_LONG)
            toast.show()
            Handler(Looper.getMainLooper()).postDelayed({ toast.cancel() }, 800)
        } else {
            finish()
            exitProcess(0)
        }
    }

    private lateinit var navController: NavController


    override fun onRestart() {
        changeOnline(true)
        super.onRestart()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        nav_view.itemIconTintList = null
        badgeSetup(R.id.navigation_chat, 50)

        userManager.userMailFlow.asLiveData().observe(this) {
            USER_EMAIL = it
            changeOnline(true)
        }

        if (!idAndTknSet) {
            idAndTknSet = true
            userManager.userIdFlow.asLiveData().observe(this) {
                USER_Id = it
            }
            GlobalScope.launch(Dispatchers.IO) {
                userManager.userTokenFlow.collect {
                    USER_TKN = it
                }
            }
        }

        userManager.userFullNameFlow.asLiveData().observe(this) {
            USER_FullName = it
        }

        nav_view.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        nav_view.setOnNavigationItemReselectedListener {
            when (it.itemId) {
                R.id.navigation_new_post -> {
                    val intent = Intent(this@MainActivity2, PublishActivity::class.java)
                    intent.putExtra("User_Token", USER_TKN)
                    startActivity(intent)
                    fm.beginTransaction().hide(active).show(home).commit()
                    nav_view.selectedItemId = R.id.navigation_home
                    true
                }
            }
        }

        fm.beginTransaction().add(R.id.nav_host_fragment, map, "map").hide(map).commit()
        fm.beginTransaction().add(R.id.nav_host_fragment, chat, "chat").hide(chat).commit()
        fm.beginTransaction().add(R.id.nav_host_fragment, profile, "profile").hide(profile).commit()
        fm.beginTransaction().add(R.id.nav_host_fragment, home, "home").hide(home).commit()
    }

    fun getTkn(): String? {
        return USER_TKN
    }

    fun getUserEmail(): String {
        return USER_EMAIL
    }


    fun getUserFullName(): String? {
        return USER_FullName
    }

    fun getUID(): Int {
        return USER_Id
    }

    private val mOnNavigationItemSelectedListener: BottomNavigationView.OnNavigationItemSelectedListener =
        object : BottomNavigationView.OnNavigationItemSelectedListener {
            override fun onNavigationItemSelected(item: MenuItem): Boolean {
                when (item.itemId) {
                    R.id.navigation_new_post -> {
                        val intent = Intent(this@MainActivity2, PublishActivity::class.java)
                        intent.putExtra("User_Token", USER_TKN)
                        startActivity(intent)
                        //fm.beginTransaction().hide(active).show(home).commit()
                        return true
                    }
                    R.id.navigation_map -> {
                        fm.beginTransaction().hide(active).show(map).commit()
                        active = map
                        return true
                    }
                    R.id.navigation_home -> {
                        fm.beginTransaction().hide(active).show(home).commit()
                        active = home
                        return true
                    }
                    R.id.navigation_chat -> {
                        fm.beginTransaction().hide(active).show(chat).commit()
                        active = chat
                        badgeClear(R.id.navigation_chat)
                        return true
                    }
                    R.id.navigation_profile -> {
                        val bundle = Bundle()
                        bundle.putString("Umail", USER_EMAIL)
                        bundle.putString("UTKN", USER_TKN)
                        Log.i("user ID", USER_Id.toString())
                        // set Fragmentclass Arguments
                        val fragobj = ProfileFragment()
                        fragobj.arguments = bundle
                        fm.beginTransaction().hide(active).show(profile).commit()
                        active = profile
                        return true
                    }
                }
                return false
            }
        }

    fun changeOnline(online: Boolean) {
        var api: ApiRequests? = null
        try {
            api = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiRequests::class.java)
        } catch (e: Exception) {
            Log.i("ApiRequest", e.toString())
            Snackbar.make(mailLoginLayout, "Ошибка подключения", Snackbar.LENGTH_SHORT).show()
        }
        val myEmail = USER_EMAIL
        GlobalScope.launch(Dispatchers.IO) {
            try {
                var token = getTkn()!!
                if (online) {
                    val response = api?.updateOnlineTrue(myEmail, token)?.awaitResponse()
                    Log.i("Response", response.toString())
                    Log.i("Response body", response?.body().toString())
                } else {
                    val response = api?.updateOnlineFalse(myEmail, token)?.awaitResponse()
                    Log.i("Response", response.toString())
                    Log.i("Response body", response?.body().toString())
                }
            } catch (e: Exception) {
                Log.i("Online change", e.toString())
                Snackbar.make(mailLoginLayout, "Ошибка сети", Snackbar.LENGTH_SHORT).show()
            }
        }
        if (online) {
            try {
                var params = IO.Options()
                params.reconnectionDelay = 1
                mOnlineSocket = IO.socket("http://37.230.114.186:7777", params)
                mOnlineSocket.on(Socket.EVENT_CONNECT, Emitter.Listener {
                    Log.d("connect", "Success")
                    mOnlineSocket.emit("userOnline", myEmail)
                })
                mOnlineSocket.connect()
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("fail", "Failed to connect")
            }

        } else {
            mOnlineSocket.emit("userOffline", myEmail)
            mOnlineSocket.disconnect()
        }
    }
}