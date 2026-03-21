package com.ohmyguide.app.ui.navi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ohmyguide.app.ui.screen.auth.AuthScreen
import com.ohmyguide.app.ui.screen.auth.AuthState
import com.ohmyguide.app.ui.screen.auth.AuthViewModel
import com.ohmyguide.app.ui.screen.onboarding.SplashDestination
import com.ohmyguide.app.ui.screen.onboarding.SplashScreen
import com.ohmyguide.app.ui.screen.onboarding.SplashViewModel
import com.ohmyguide.app.ui.screen.onboarding.CategoryScreen
import com.ohmyguide.app.ui.screen.onboarding.GpsPermissionScreen
import com.ohmyguide.app.ui.screen.onboarding.LoadingScreen
import com.ohmyguide.app.ui.screen.onboarding.WelcomeScreen
import com.ohmyguide.app.ui.screen.explore.ExploreScreen
import com.ohmyguide.app.ui.screen.home.HomeScreen
import com.ohmyguide.app.ui.screen.map.MapScreen
import com.ohmyguide.app.ui.screen.mypage.MyPageScreen
import com.ohmyguide.app.ui.screen.navi.NaviScreen
import com.ohmyguide.app.ui.screen.phrases.PhrasesScreen
import com.ohmyguide.app.ui.screen.place.PlaceScreen
import com.ohmyguide.app.ui.screen.transport.TransitDetailScreen
import com.ohmyguide.app.ui.screen.transport.TransportPickerScreen

@Composable
fun NavGraph(navController: NavHostController, onNaviMinimize: (placeId: String, mode: String) -> Unit = { _, _ -> }) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            val splashViewModel: SplashViewModel = hiltViewModel()
            val destination by splashViewModel.destination.collectAsState()

            SplashScreen(
                onFinish = {
                    val target = when (destination) {
                        SplashDestination.Home -> Screen.Home.createRoute()
                        else -> Screen.Welcome.route
                    }
                    navController.navigate(target) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
            )
        }
        composable(Screen.Welcome.route) {
            val authViewModel: AuthViewModel = hiltViewModel()
            val authState by authViewModel.authState.collectAsState()
            val context = LocalContext.current
            // CredentialManager requires Activity context
            val activityContext = context as? android.app.Activity ?: context

            LaunchedEffect(authState) {
                if (authState is AuthState.Success) {
                    navController.navigate(Screen.GpsPermission.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }
            }

            WelcomeScreen(
                onSignIn = { authViewModel.signInWithGoogle(activityContext) },
                authState = authState,
                onDismissError = { authViewModel.resetState() },
            )
        }
        composable(Screen.Login.route) { AuthScreen(navController) }
        composable(Screen.GpsPermission.route) {
            GpsPermissionScreen(
                onAllow = {
                    navController.navigate(Screen.InterestSelect.route) {
                        popUpTo(Screen.GpsPermission.route) { inclusive = true }
                    }
                },
            )
        }
        composable(Screen.InterestSelect.route) {
            CategoryScreen(
                onConfirm = { selectedCategories ->
                    val categoryStr = selectedCategories.joinToString(",")
                    navController.navigate("loading?category=$categoryStr") {
                        popUpTo(Screen.InterestSelect.route) { inclusive = true }
                    }
                },
            )
        }
        composable("loading?category={category}") { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: ""
            LoadingScreen(
                onFinish = {
                    navController.navigate(Screen.Home.createRoute(category)) {
                        popUpTo("loading?category={category}") { inclusive = true }
                    }
                },
            )
        }

        composable(Screen.Home.route) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: ""
            HomeScreen(navController, category = category)
        }
        composable(Screen.Map.route) { MapScreen(navController) }
        composable(Screen.Explore.route) { ExploreScreen(navController) }
        composable(Screen.Phrases.route) { PhrasesScreen(navController) }
        composable(Screen.MyPage.route) { MyPageScreen(navController) }

        composable(Screen.CourseDetail.route) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId") ?: return@composable
            com.ohmyguide.app.ui.screen.explore.CourseDetailScreen(navController, courseId)
        }
        composable(Screen.CourseNavi.route) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId") ?: return@composable
            com.ohmyguide.app.ui.screen.explore.CourseNaviScreen(navController, courseId)
        }
        composable(Screen.Place.route) { backStackEntry ->
            val placeId = backStackEntry.arguments?.getString("placeId") ?: return@composable
            PlaceScreen(navController, placeId)
        }
        composable(Screen.Transport.route) { backStackEntry ->
            val placeId = backStackEntry.arguments?.getString("placeId") ?: return@composable
            TransportPickerScreen(navController, placeId)
        }
        composable(Screen.TransitDetail.route) { backStackEntry ->
            val placeId = backStackEntry.arguments?.getString("placeId") ?: return@composable
            TransitDetailScreen(navController, placeId)
        }
        composable(Screen.Navi.route) { backStackEntry ->
            val placeId = backStackEntry.arguments?.getString("placeId") ?: return@composable
            val mode = backStackEntry.arguments?.getString("mode") ?: "walk"
            NaviScreen(
                navController = navController,
                placeId = placeId,
                mode = mode,
                onMinimize = {
                    onNaviMinimize(placeId, mode)
                    navController.navigate(Screen.Place.createRoute(placeId)) {
                        popUpTo(Screen.Home.route)
                    }
                },
            )
        }
    }
}
