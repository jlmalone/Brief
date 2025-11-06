package com.techventus.wikipedianews.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.techventus.wikipedianews.ui.compose.screen.bookmarks.BookmarksScreen
import com.techventus.wikipedianews.ui.compose.screen.news.NewsScreen
import com.techventus.wikipedianews.ui.compose.screen.settings.SettingsScreen

/**
 * Navigation routes for the app
 */
object Routes {
    const val NEWS = "news"
    const val BOOKMARKS = "bookmarks"
    const val SETTINGS = "settings"
}

/**
 * Main navigation graph
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Routes.NEWS
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.NEWS) {
            NewsScreen(
                onNavigateToSettings = {
                    navController.navigate(Routes.SETTINGS)
                }
            )
        }

        composable(Routes.BOOKMARKS) {
            BookmarksScreen()
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
