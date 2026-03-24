package com.ohmyguide.app.ui.navi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.ohmyguide.app.ui.common.NavMinimizedState
import com.ohmyguide.app.ui.screen.auth.AuthScreen
import com.ohmyguide.app.ui.screen.auth.AuthState
import com.ohmyguide.app.ui.screen.auth.AuthViewModel
import com.ohmyguide.app.ui.screen.onboarding.SplashScreen
import com.ohmyguide.app.ui.screen.onboarding.CategoryScreen
import com.ohmyguide.app.ui.screen.onboarding.GpsPermissionScreen
import com.ohmyguide.app.ui.screen.onboarding.LoadingScreen
import com.ohmyguide.app.ui.screen.onboarding.OnboardingHelper
import com.ohmyguide.app.ui.screen.onboarding.WelcomeScreen
import com.ohmyguide.app.data.repository.UserRepository
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
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
fun NavGraph(
    navController: NavHostController,
    onNaviMinimize: (placeId: String, mode: String) -> Unit = { _, _ -> },
    onNaviStart: () -> Unit = {},
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onFinish = {
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
            )
        }
        composable(Screen.Welcome.route) {
            val authViewModel: AuthViewModel = hiltViewModel()
            val authState by authViewModel.authState.collectAsState()
            val context = LocalContext.current

            LaunchedEffect(authState) {
                if (authState is AuthState.Success) {
                    authViewModel.resetState()
                    navController.navigate(Screen.GpsPermission.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }
            }

            WelcomeScreen(
                onSignIn = { authViewModel.signInWithGoogle(context) },
                authState = authState,
                onDismissError = { authViewModel.resetState() },
            )
        }
        composable(Screen.Login.route) { AuthScreen(navController) }
        composable(Screen.GpsPermission.route) {
            val userRepository: UserRepository = hiltViewModel<OnboardingHelper>().userRepository
            val scope = rememberCoroutineScope()

            GpsPermissionScreen(
                onAllow = { gender, age, country, companion ->
                    scope.launch {
                        userRepository.completeOnboarding(country, age, gender, companion, country)
                    }
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
        composable(
            route = Screen.TransitDetail.route,
            arguments = listOf(
                navArgument("destLat") { type = NavType.StringType; defaultValue = "0.0" },
                navArgument("destLng") { type = NavType.StringType; defaultValue = "0.0" },
            ),
        ) { backStackEntry ->
            val placeId = backStackEntry.arguments?.getString("placeId") ?: return@composable
            TransitDetailScreen(navController, placeId)
        }
        composable(Screen.Navi.route) { backStackEntry ->
            val placeId = backStackEntry.arguments?.getString("placeId") ?: return@composable
            val mode = backStackEntry.arguments?.getString("mode") ?: "walk"

            LaunchedEffect(placeId, mode) {
                onNaviStart()
            }

            NaviScreen(
                navController = navController,
                placeId = placeId,
                mode = mode,
                onMinimize = {
                    onNaviMinimize(placeId, mode)
                    navController.popBackStack()
                },
            )
        }
    }
}