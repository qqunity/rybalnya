package com.example.rybalnya.utils

import android.content.Context
import androidx.datastore.preferences.createDataStore
import androidx.datastore.preferences.edit
import androidx.datastore.preferences.preferencesKey
import kotlinx.coroutines.flow.map

class UserManager(context: Context) {
    private val dataStore = context.createDataStore(name = "user_prefs")

    companion object {
        val USER_LOGGED_IN_KEY = preferencesKey<Boolean>("USER_LOGGED_IN")
        val USER_ID = preferencesKey<Int>("USER_ID")
        val USER_MAIL = preferencesKey<String>("USER_MAIL")
        val USER_NICK = preferencesKey<String>("USER_NICK")
        val USER_FULLNAME = preferencesKey<String>("USER_FULLNAME")
        val USER_BIO = preferencesKey<String>("USER_BIO")
        val USER_TOKEN = preferencesKey<String>("USER_TOKEN")
    }

    suspend fun storeUser(logged: Boolean, mail: String) {
        dataStore.edit {
            it[USER_LOGGED_IN_KEY] = logged
            it[USER_MAIL] = mail
        }
    }

    suspend fun editNick(nickname: String = "") {
        dataStore.edit {
            it[USER_NICK] = nickname
        }
    }

    suspend fun editID(id: Int) {
        dataStore.edit {
            it[USER_ID] = id
        }
    }

    suspend fun editFullName(fullName: String = "") {
        dataStore.edit {
            it[USER_FULLNAME] = fullName
        }
    }

    suspend fun editBio(biography: String = "") {
        dataStore.edit {
            it[USER_BIO] = biography
        }
    }

    suspend fun editToken(token: String = "") {
        dataStore.edit {
            it[USER_TOKEN] = token
        }
    }


    val userMailFlow = dataStore.data.map {
        it[USER_MAIL] ?: ""
    }
    val userIdFlow = dataStore.data.map {
        it[USER_ID] ?: 0
    }
    val userNickFlow = dataStore.data.map {
        it[USER_NICK] ?: ""
    }
    val userFullNameFlow = dataStore.data.map {
        it[USER_FULLNAME] ?: ""
    }
    val userBioFlow = dataStore.data.map {
        it[USER_BIO] ?: ""
    }
    val userTokenFlow = dataStore.data.map {
        it[USER_TOKEN] ?: ""
    }
    val userLoggedIn = dataStore.data.map {
        it[USER_LOGGED_IN_KEY] ?: false
    }
}