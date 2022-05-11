package com.training.tracker.util

import android.content.Context
import android.content.Context.MODE_PRIVATE
import androidx.core.content.edit
import com.training.tracker.R

object PreferencesUtils {


    fun Context.setCachedEmail(email: String?) {
        getSharedPreferences(getString(R.string.preference_file_key), MODE_PRIVATE).edit {
            putString(getString(R.string.saved_email_key), email)
        }
    }

    fun Context.getCachedEmail(): String? {
        return getSharedPreferences(getString(R.string.preference_file_key), MODE_PRIVATE)
            .getString(getString(R.string.saved_email_key), null)
    }
}