package com.ohmyguide.app.ui.navi

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ohmyguide.app.ui.screen.auth.AuthScreen
import com.ohmyguide.app.ui.screen.explore.ExploreScreen
import com.ohmyguide.app.ui.screen.home.HomeScreen
import com.ohmyguide.app.ui.screen.map.MapScreen
import com.ohmyguide.app.ui.screen.mypage.MyPageScreen
import com.ohmyguide.app.ui.screen.navi.NaviScreen
import com.ohmyguide.app.ui.screen.phrases.PhrasesScreen
import com.ohmyguide.app.ui.screen.place.PlaceScreen
import com.ohmyguide.app.ui.screen.story.StoryScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) { AuthScreen(navController) }
        composable(Screen.Welcome.route) { AuthScreen(navController) }
        composable(Screen.Login.route) { AuthScreen(navController) }
        composable(Screen.GpsPermission.route) { AuthScreen(navController) }
        composable(Screen.InterestSelect.route) { AuthScreen(navController) }

        composable(Screen.Home.route) { HomeScreen(navController) }
        composable(Screen.Map.route) { MapScreen(navController) }
        composable(Screen.Explore.route) { ExploreScreen(navController) }
        composable(Screen.Phrases.route) { PhrasesScreen(navController) }
        composable(Screen.MyPage.route) { MyPageScreen(navController) }

        composable(Screen.Place.route) { backStackEntry ->
            val placeId = backStackEntry.arguments?.getString("placeId") ?: return@composable
            PlaceScreen(navController, placeId)
        }
        composable(Screen.Navi.route) { backStackEntry ->
            val placeId = backStackEntry.arguments?.getString("placeId") ?: return@composable
            NaviScreen(navController, placeId)
        }
        composable(Screen.Story.route) { backStackEntry ->
            val placeId = backStackEntry.arguments?.getString("placeId") ?: return@composable
            StoryScreen(navController, placeId)
        }
    }
}
