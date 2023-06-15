package com.bayutb.gombti.ui.register

import android.content.Context
import android.content.SharedPreferences
import com.bayutb.gombti.R

class RegisterSessionManager(context: Context) {
    private var prefs: SharedPreferences =
        context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)

    fun saveSessionFromMainActivity(userId: String) {
        val editor = prefs.edit()
        editor.putString(USER_ID_MAIN_ACTIVITY, userId)
        editor.apply()
    }

    fun loadSessionFromMainActivity() : String? {
        return prefs.getString(USER_ID_MAIN_ACTIVITY, null)
    }

    fun clearSessionFromMainActivity() {
        val editor = prefs.edit()
        editor.remove(USER_ID_MAIN_ACTIVITY)
        editor.apply()
    }

    companion object {
        private const val USER_ID_MAIN_ACTIVITY = "user_name_from_main_activity"
    }
}