package com.aircalc.converter.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Manages disclaimer acceptance state using DataStore.
 *
 * This class handles persistence of the user's acceptance of the app's terms and conditions.
 * The disclaimer must be shown and accepted on first launch before the user can access the app.
 *
 * ## Usage:
 * ```kotlin
 * val disclaimerPrefs = DisclaimerPreferences(context)
 *
 * // Check if terms have been accepted
 * disclaimerPrefs.isDisclaimerAccepted.collect { accepted ->
 *     if (!accepted) showDisclaimerScreen()
 * }
 *
 * // Save acceptance
 * disclaimerPrefs.setDisclaimerAccepted()
 * ```
 *
 * @param context Application context for accessing DataStore
 */
class DisclaimerPreferences(private val context: Context) {

    companion object {
        private val Context.disclaimerDataStore by preferencesDataStore(name = "disclaimer_prefs")
        private val KEY_TERMS_ACCEPTED = booleanPreferencesKey("terms_accepted")
        private val KEY_TERMS_ACCEPTED_DATE = longPreferencesKey("terms_accepted_date")
    }

    /**
     * Flow that emits true if the disclaimer has been accepted, false otherwise.
     */
    val isDisclaimerAccepted: Flow<Boolean> = context.disclaimerDataStore.data
        .map { preferences ->
            preferences[KEY_TERMS_ACCEPTED] ?: false
        }

    /**
     * Save the user's acceptance of the disclaimer.
     * Records both the acceptance flag and the timestamp.
     */
    suspend fun setDisclaimerAccepted() {
        context.disclaimerDataStore.edit { preferences ->
            preferences[KEY_TERMS_ACCEPTED] = true
            preferences[KEY_TERMS_ACCEPTED_DATE] = System.currentTimeMillis()
        }
    }
}
