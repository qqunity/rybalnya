package com.example.rybalnya

import android.Manifest
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.asLiveData
import androidx.lifecycle.observe
import com.example.rybalnya.api.ApiRequests
import com.example.rybalnya.api.UserJSON
import com.example.rybalnya.utils.UserManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import retrofit2.Retrofit
import retrofit2.awaitResponse
import retrofit2.converter.gson.GsonConverterFactory
import java.math.BigInteger
import java.security.MessageDigest
import kotlin.system.exitProcess

fun String.sha256(): String {
    val md = MessageDigest.getInstance("SHA-256")
    return BigInteger(1, md.digest(toByteArray())).toString(16).padStart(32, '0')
}

const val BASE_URL = "http://37.230.114.186:8000"
const val WEATHER_URL = "http://api.openweathermap.org/data/2.5/"
const val WEATHER_API_KEY = "65ba5faf4eff3b299bd8dd39a7182789"
//const val BASE_URL = "http://192.168.1.37:8000"

class MainActivity : AppCompatActivity() {

    private lateinit var userManager: UserManager
    private var email = ""
    private var loggedIn = false


    private fun goToAnotherActivity() {
        val intent = Intent(this, MainActivity2::class.java)
        startActivity(intent)
        finish()
    }

    private inline fun startCoroutineTimer(
        delayMillis: Long = 2000,
        crossinline action: () -> Unit
    ) = GlobalScope.launch(Dispatchers.Unconfined) {
        delay(delayMillis)
        Log.i("timer", "backWasPressedOnLogin = false")
        action()
    }

    private var backWasPressedOnLogin = false

    private fun closeKeyBoard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    override fun onBackPressed() {

        if (startLayout?.visibility == View.GONE && mailLoginLayout?.visibility == View.VISIBLE) {

            startLayout.visibility = View.VISIBLE

            mailLoginLayout.animate().alpha(0f).duration = 200
            backToLoginScreen.animate().alpha(0f).duration = 200

            startLayout.animate().alpha(1f).duration = 400

            emailAddressEnter.setText("")
            passEnter.setText("")
            backToLoginScreen.visibility = View.GONE
            mailLoginLayout.visibility = View.GONE
        } else if (startLayout?.visibility == View.GONE && signUpLayout?.visibility == View.VISIBLE) {

            startLayout.visibility = View.VISIBLE

            signUpLayout.animate().alpha(0f).duration = 200
            backToLoginScreen.animate().alpha(0f).duration = 200

            startLayout.animate().alpha(1f).duration = 400

            emailAddressSignUp.setText("")
            passSignUp.setText("")
            passRepeat.setText("")

            backToLoginScreen.visibility = View.GONE
            signUpLayout.visibility = View.GONE
        } else {
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
    }

    private fun dataCorrect(mail: String, pass: String, passConfirm: String = pass): Boolean {
        if (mail.trim().isEmpty()) {
            Snackbar.make(signUpLayout, "Введите почту", Snackbar.LENGTH_SHORT).show()
            return false
        } else if (pass.trim().isEmpty()) {
            Snackbar.make(signUpLayout, "Введите пароль", Snackbar.LENGTH_SHORT).show()
            return false
        } else if (passConfirm.trim().isEmpty()) {
            Snackbar.make(signUpLayout, "Подтвердите пароль", Snackbar.LENGTH_SHORT).show()
            return false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(mail).matches()) {
            Snackbar.make(signUpLayout, "Несуществующий e-mail", Snackbar.LENGTH_SHORT).show()
            return false
        } else if (pass != passConfirm) {
            Snackbar.make(signUpLayout, "Пароли не совпадают", Snackbar.LENGTH_SHORT).show()
            return false
        } else if (pass.trim().length < 6) {
            Snackbar.make(signUpLayout, "Минимальная длина 6", Snackbar.LENGTH_SHORT).show()
            return false
        } else {
            return true
        }

    }

    private fun signUp(mail: String, pass: String, passConfirm: String) {
        if (dataCorrect(mail, pass, passConfirm)) {
            val hash = pass.sha256()
            val user = UserJSON(null, "", mail, "", null, "", hash)
            var api: ApiRequests? = null
            try {
                api = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(ApiRequests::class.java)
            } catch (e: Exception) {
                Log.i("ApiRequest", e.toString())
                Snackbar.make(signUpLayout, "Ошибка подключения", Snackbar.LENGTH_SHORT).show()
            }
            GlobalScope.launch {
                try {
                    val response = api?.signUpUser(user)?.awaitResponse()
                    Log.i("Response code", response?.code().toString())
                    if (response?.code() == 201) {
                        val data = response.body()!!
                        response.body()!!.AccessToken?.let { userManager.editToken(it) }
                        response.body()!!.UserID?.let { userManager.editID(it) }
                        Log.i("Register", "Successful")
                        Log.i("Register", data.toString())
                        userManager.storeUser(true, mail)
                        goToAnotherActivity()
                    } else if (response?.code() == 400) {
                        Snackbar.make(
                            signUpLayout,
                            response.body()?.Error.toString(),
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    Log.i("Register", e.toString())
                    Snackbar.make(signUpLayout, "Ошибка сети", Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun login(mail: String, pass: String) {
        if (dataCorrect(mail, pass)) {
            val hash = pass.sha256()
            val user = UserJSON(null, "", mail, "", null, "", hash)
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
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = api?.loginUser(user)?.awaitResponse()
                    Log.i("Response", response.toString())
                    Log.i("Response body", response?.body().toString())
                    if (response?.code() == 200) {
                        withContext(Dispatchers.Main) {
                            response.body()!!.AccessToken?.let { userManager.editToken(it) }
                        }
                        Log.i("Login", "Successful")
                        Log.i("Login", response.body()!!.toString())
                        val resp =
                            response.body()!!.AccessToken?.let {
                                api?.getUserInfoByEmail(
                                    mail,
                                    it
                                )?.awaitResponse()
                            }
                        userManager.storeUser(true, mail)
                        Log.i("getUserInfoByEmail", resp?.body().toString())
                        resp?.body()!!.UserInfo?.Nickname?.let { userManager.editNick(it) }
                        resp.body()!!.UserInfo?.ID?.let { userManager.editID(it) }
                        resp.body()!!.UserInfo?.FullName?.let { userManager.editFullName(it) }
                        resp.body()!!.UserInfo?.About?.let { userManager.editBio(it) }
                        resp.body()!!.UserInfo?.Image?.let { s ->
                            Log.i("image", s)
                            openFileOutput("avatar.png", Context.MODE_PRIVATE).use {
                                it?.write(Base64.decode(s, 0))
                            }
                        }
                        goToAnotherActivity()
                    } else if (response?.code() == 400) { //TODO("check response")
                        Snackbar.make(
                            mailLoginLayout,
                            response.body()?.Error.toString(),
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    Log.i("Login", e.toString())
                    Snackbar.make(mailLoginLayout, "Ошибка сети", Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun observeData() {
        userManager.userMailFlow.asLiveData().observe(this) {
            email = it
        }
        userManager.userLoggedIn.asLiveData().observe(this) {
            loggedIn = it
            if (loggedIn) {
                goToAnotherActivity()
            } else if (!loggedIn) {
                progressBar.visibility = View.GONE
                startLayout.visibility = View.VISIBLE
            }
        }
    }

    private fun verifyPermissions(activity: Activity) {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                activity,
                ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermission()
        }
    }

    private fun requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            || ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                ACCESS_FINE_LOCATION
            )
        ) {
            AlertDialog.Builder(this).setTitle("Permission needed").setMessage("Needed for avatar")
                .setPositiveButton("ok") { dialog, which ->
                    ActivityCompat.requestPermissions(
                        MainActivity(),
                        arrayOf(
                            Manifest.permission.READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE,
                            ACCESS_FINE_LOCATION
                        ),
                        1
                    )
                    Toast.makeText(
                        applicationContext,
                        "thanks", Toast.LENGTH_SHORT
                    ).show()
                }.setNegativeButton("Cancel") { dialog, which ->
                    dialog.dismiss()
                }.create().show()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE,
                    ACCESS_FINE_LOCATION
                ),
                1
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        verifyPermissions(this)

        userManager = UserManager(this)

        observeData()

        container?.setOnClickListener {
            closeKeyBoard()
        }

        backToLoginScreen?.setOnClickListener {
            this.onBackPressed()
        }

        googleLogin?.setOnClickListener {
            val toast = Toast.makeText(this, "Hi there, i'm not implemented yet", Toast.LENGTH_LONG)
            toast.show()
            Handler(Looper.getMainLooper()).postDelayed({ toast.cancel() }, 1000)
        }

        vkLogin?.setOnClickListener {
            val toast = Toast.makeText(this, "Hi there, i'm not implemented yet", Toast.LENGTH_LONG)
            toast.show()
            Handler(Looper.getMainLooper()).postDelayed({ toast.cancel() }, 1000)
        }

        mailLogin?.setOnClickListener {
            mailLoginLayout.visibility = View.VISIBLE
            backToLoginScreen.visibility = View.VISIBLE

            startLayout.animate().alpha(0f).duration = 200
            startLayout.visibility = View.GONE

            mailLoginLayout.animate().alpha(1f).duration = 400
            backToLoginScreen.animate().alpha(1f).duration = 400
        }

        areNotwithUs?.setOnClickListener {

            emailAddressEnter.setText("")
            passEnter.setText("")

            signUpLayout.visibility = View.VISIBLE

            mailLoginLayout.animate().alpha(0f).duration = 200
            mailLoginLayout.visibility = View.GONE

            signUpLayout.animate().alpha(1f).duration = 400
        }

        enterButton?.setOnClickListener {
            login(
                emailAddressEnter.text.toString(),
                passEnter.text.toString()
            )
/*            GlobalScope.launch(Dispatchers.IO) {
                userManager.storeUser(true, "a@a.a")
            }*/
        }

        signUpButton?.setOnClickListener {
            signUp(
                emailAddressSignUp.text.toString(),
                passSignUp.text.toString(),
                passRepeat.text.toString()
            )
        }
    }
}