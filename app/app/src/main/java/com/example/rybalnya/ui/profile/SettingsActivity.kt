package com.example.rybalnya.ui.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.asLiveData
import androidx.lifecycle.observe
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.example.rybalnya.BASE_URL
import com.example.rybalnya.MainActivity
import com.example.rybalnya.R
import com.example.rybalnya.api.ApiRequests
import com.example.rybalnya.api.UserRecieve
import com.example.rybalnya.utils.UserManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.awaitResponse
import retrofit2.converter.gson.GsonConverterFactory


class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()
        val toolbar: Toolbar = findViewById<View>(R.id.top_settings_bar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    class SettingsFragment : PreferenceFragmentCompat() {

        private lateinit var userManager: UserManager

        private lateinit var userMail: String
        private lateinit var userToken: String

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
//            addPreferencesFromResource(R.xml.pref_button)
            userManager = UserManager(this.requireActivity())
            userManager.userMailFlow.asLiveData().observe(this.requireActivity()) {
                userMail = it
            }
            userManager.userTokenFlow.asLiveData().observe(this.requireActivity()) {
                userToken = it
            }

            setFields()

            val nickPreference: EditTextPreference? = findPreference("nickname")
            val fullnamePreference: EditTextPreference? = findPreference("fullname")
            val bioPreference: EditTextPreference? = findPreference("biography")
            val deleteButton: Preference? = findPreference("deleteButton")

            nickPreference?.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { preference: Preference, value: Any ->
                    updateUserInfo(userToken, userMail, value.toString())
                    GlobalScope.launch(Dispatchers.IO) {
                        userManager.editNick(value.toString())
                    }
                    true
                }
            fullnamePreference?.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { preference: Preference, value: Any ->
                    updateUserInfo(userToken, userMail, null, value.toString())
                    GlobalScope.launch(Dispatchers.IO) {
                        userManager.editFullName(value.toString())
                    }
                    true
                }
            bioPreference?.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { preference: Preference, value: Any ->
                    updateUserInfo(userToken, userMail, null, null, value.toString())
                    GlobalScope.launch(Dispatchers.IO) {
                        userManager.editBio(value.toString())
                    }
                    true
                }

            deleteButton?.setOnPreferenceClickListener {
                Log.i("deleteButton", "I was pressed")
                deleteUser(userMail, userToken)
                true
            }

        }

//        TODO("Сказать Денису сделать проверку на несанкционированное удаление чужих данных(другого пользователя, чужие посты, комментарии и лайки)")

        private fun setFields() {
            val nickPreference: EditTextPreference? = findPreference("nickname")
            val fullnamePreference: EditTextPreference? = findPreference("fullname")
            val bioPreference: EditTextPreference? = findPreference("biography")
            userManager.userNickFlow.asLiveData().observe(this) {
                nickPreference?.summaryProvider =
                    Preference.SummaryProvider<EditTextPreference> { preference ->
                        val text = preference.text
                        if (it == "" && text == "") {
                            "Введите ник"
                        } else {
                            it
                        }
                    }
            }
            userManager.userFullNameFlow.asLiveData().observe(this) {
                fullnamePreference?.summaryProvider =
                    Preference.SummaryProvider<EditTextPreference> { preference ->
                        val text = preference.text
                        if (it == "" && text == "") {
                            "Введите имя и фамилию"
                        } else {
                            it
                        }
                    }
            }
            userManager.userBioFlow.asLiveData().observe(this) {
                bioPreference?.summaryProvider =
                    Preference.SummaryProvider<EditTextPreference> { preference ->
                        val text = preference.text
                        if (it == "" && text == "") {
                            "Расскажите о себе"
                        } else {
                            (it.take(80) + "...")
                        }
                    }
            }

        }

        private fun deleteUser(mail: String, token: String) {
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
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = api?.deleteUserByEmail(mail, token)?.awaitResponse()
                    Log.i("Response", response.toString())
                    Log.i("Response body", response?.body().toString())
                    if (response?.code() == 200) {
                        Log.i("Delete", "Successful")
                    } else if (response?.code() == 400) {
                        Log.i("Delete", "Something went wrong")
                    }
                } catch (e: Exception) {
                    Log.i("Login", e.toString())
                }
            }

            userManager = UserManager(this.requireActivity())
            GlobalScope.launch(Dispatchers.IO) {
                userManager.storeUser(false, "")
                userManager.editBio("")
                userManager.editNick("")
                userManager.editFullName("")
                userManager.editToken("")
            }
            val intent = Intent(activity, MainActivity::class.java)
            startActivity(intent)
            activity?.finish()

        }

        private fun updateUserInfo(
            token: String,
            mail: String,
            nick: String? = null,
            fullName: String? = null,
            about: String? = null
        ) {
            val user = UserRecieve(null, about, mail, fullName, null, nick, null)
            Log.i("userJSON", user.toString())
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
                    val response = api?.updateUser(user, token)?.awaitResponse()
                    Log.i("Response", response.toString())
                    Log.i("Response body", response?.body().toString())
                    if (response?.code() == 200) {
                        Log.i("Update", "Successful")
                        Toast.makeText(
                            this@SettingsFragment.requireActivity(),
                            response.body()?.Action.toString(),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else if (response?.code() == 400) {
                        Log.i("Update", "Something went wrong")
                        Toast.makeText(
                            this@SettingsFragment.requireActivity(),
                            response.body()?.Error.toString(),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    Log.i("Login", e.toString())
                }
            }
        }
    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

}