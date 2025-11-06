package com.techventus.wikipedianews.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.techventus.wikipedianews.model.datastore.UserPreferencesDataStore
import com.techventus.wikipedianews.ui.compose.MainScreen
import com.techventus.wikipedianews.ui.theme.BriefTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Main activity for Brief app.
 *
 * Following android-template pattern:
 * - ComponentActivity for Compose
 * - @AndroidEntryPoint for Hilt injection
 * - Bottom navigation with MainScreen
 * - Dark theme from DataStore preferences
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userPreferencesDataStore: UserPreferencesDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val preferences by userPreferencesDataStore.userPreferencesFlow
                .collectAsStateWithLifecycle(
                    initialValue = UserPreferencesDataStore.UserPreferences()
                )

            BriefTheme(darkTheme = preferences.isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}
