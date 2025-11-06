package com.techventus.wikipedianews.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.techventus.wikipedianews.ui.compose.screen.news.NewsScreen
import com.techventus.wikipedianews.ui.theme.BriefTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main activity for Brief app.
 *
 * Following android-template pattern:
 * - ComponentActivity for Compose
 * - @AndroidEntryPoint for Hilt injection
 * - Minimal activity with Compose content only
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BriefTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NewsScreen()
                }
            }
        }
    }
}
