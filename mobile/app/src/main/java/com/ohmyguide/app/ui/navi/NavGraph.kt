package com.ohmyguide.app.ui.navi

import android.widget.Toast
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
import com.ohmyguide.app.ui.screen.onboarding.SplashDestination
import com.ohmyguide.app.ui.screen.onboarding.SplashScreen
import com.ohmyguide.app.ui.screen.onboarding.SplashViewModel
import com.ohmyguide.app.ui.screen.onboarding.CategoryScreen
import com.ohmyguide.app.ui.screen.onboarding.GpsPermissionScreen
import com.ohmyguide.app.ui.screen.onboarding.LoadingScreen
import com.ohmyguide.app.ui.screen.onboarding.OnboardingHelper
import com.ohmyguide.app.ui.screen.onboarding.WelcomeScreen
import com.ohmyguide.app.ui.theme.LocalStrings
import com.ohmyguide.app.data.repository.UserRepository
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.ohmyguide.app.ui.screen.explore.ExploreScreen
import com.ohmyguide.app.ui.screen.home.HomeScreen
import com.ohmyguide.app.ui.screen.map.MapScreen
import com.ohmyguide.app.ui.screen.mypage.MyPageScreen
import com.ohmyguide.app.ui.screen.navi.NaviScreen
import com.ohmyguide.app.ui.screen.phrases.PhrasesScreen
import com.ohmyguide.app.ui.screen.rating.RatingScreen
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
            val splashViewModel: SplashViewModel = hiltViewModel()
            val destination by splashViewModel.destination.collectAsState()

            SplashScreen(
                onFinish = {
                    val route = when (destination) {
                        is SplashDestination.Home -> Screen.Home.createRoute()
                        is SplashDestination.Onboarding -> Screen.GpsPermission.route
                        else -> Screen.Welcome.route
                    }
                    navController.navigate(route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
            )
        }
        composable(Screen.Welcome.route) {
            val authViewModel: AuthViewModel = hiltViewModel()
            val authState by authViewModel.authState.collectAsState()
            val context = LocalContext.current
            val userRepository: UserRepository = hiltViewModel<OnboardingHelper>().userRepository
            val scope = rememberCoroutineScope()

            val strings = LocalStrings.current

            LaunchedEffect(authState) {
                if (authState is AuthState.Success) {
                    Toast.makeText(context, strings.loginSuccess, Toast.LENGTH_SHORT).show()
                    authViewModel.resetState()

                    scope.launch {
                        val dest = userRepository.getCurrentUser().fold(
                            onSuccess = { user ->
                                if (user.onboardingCompleted) Screen.InterestSelect.route
                                else Screen.GpsPermission.route
                            },
                            onFailure = { Screen.GpsPermission.route },
                        )
                        navController.navigate(dest) {
                            popUpTo(Screen.Welcome.route) { inclusive = true }
                        }
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
                        userRepository.completeOnboarding(country, age, gender, companion, "active")
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
        composable(
            route = "loading?category={category}",
            arguments = listOf(
                navArgument("category") { type = NavType.StringType; defaultValue = "" },
            ),
        ) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: ""
            LoadingScreen(
                onFinish = {
                    navController.navigate(Screen.Home.createRoute(category)) {
                        popUpTo("loading?category={category}") { inclusive = true }
                    }
                },
            )
        }

        composable(
            route = Screen.Home.route,
            arguments = listOf(
                navArgument("category") { type = NavType.StringType; defaultValue = "" },
            ),
        ) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: ""
            HomeScreen(navController, category = category)
        }
        composable(Screen.Map.route) { MapScreen(navController) }
        composable(Screen.Explore.route) { ExploreScreen(navController) }
        composable(Screen.Phrases.route) { PhrasesScreen(navController) }
        composable(Screen.MyPage.route) { MyPageScreen(navController) }

        composable(
            route = Screen.CourseDetail.route,
            arguments = listOf(
                navArgument("courseId") { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId") ?: return@composable
            com.ohmyguide.app.ui.screen.explore.CourseDetailScreen(navController, courseId)
        }
        composable(
            route = Screen.CourseNavi.route,
            arguments = listOf(
                navArgument("courseId") { type = NavType.StringType },
                navArgument("mode") { type = NavType.StringType; defaultValue = "car" },
            ),
        ) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId") ?: return@composable
            val mode = backStackEntry.arguments?.getString("mode") ?: "car"
            com.ohmyguide.app.ui.screen.explore.CourseNaviScreen(navController, courseId, mode)
        }
        composable(
            route = Screen.Place.route,
            arguments = listOf(
                navArgument("placeId") { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val placeId = backStackEntry.arguments?.getString("placeId") ?: return@composable
            PlaceScreen(navController, placeId)
        }
        composable(
            route = Screen.Transport.route,
            arguments = listOf(
                navArgument("placeId") { type = NavType.StringType },
                navArgument("courseId") { type = NavType.StringType; defaultValue = "" },
                navArgument("spotIndex") { type = NavType.StringType; defaultValue = "0" },
            ),
        ) { backStackEntry ->
            val placeId = backStackEntry.arguments?.getString("placeId") ?: return@composable
            val courseId = backStackEntry.arguments?.getString("courseId")?.ifEmpty { null }
            val spotIndex = backStackEntry.arguments?.getString("spotIndex")?.toIntOrNull() ?: 0
            TransportPickerScreen(navController, placeId, courseId = courseId, spotIndex = spotIndex)
        }
        composable(
            route = Screen.TransitDetail.route,
            arguments = listOf(
                navArgument("placeId") { type = NavType.StringType },
                navArgument("destLat") { type = NavType.StringType; defaultValue = "0.0" },
                navArgument("destLng") { type = NavType.StringType; defaultValue = "0.0" },
                navArgument("courseId") { type = NavType.StringType; defaultValue = "" },
            ),
        ) { backStackEntry ->
            val placeId = backStackEntry.arguments?.getString("placeId") ?: return@composable
            val courseId = backStackEntry.arguments?.getString("courseId")?.ifEmpty { null }
            TransitDetailScreen(navController, placeId, courseId = courseId)
        }
        composable(
            route = Screen.Rating.route,
            arguments = listOf(
                navArgument("placeId") { type = NavType.StringType },
                navArgument("placeName") { type = NavType.StringType; defaultValue = "" },
            ),
        ) { backStackEntry ->
            val placeId = backStackEntry.arguments?.getString("placeId") ?: return@composable
            RatingScreen(navController)
        }
        composable(
            route = Screen.Navi.route,
            arguments = listOf(
                navArgument("placeId") { type = NavType.StringType },
                navArgument("mode") { type = NavType.StringType; defaultValue = "walk" },
                navArgument("courseId") { type = NavType.StringType; defaultValue = "" },
                navArgument("spotIndex") { type = NavType.StringType; defaultValue = "0" },
            ),
        ) { backStackEntry ->
            val placeId = backStackEntry.arguments?.getString("placeId") ?: return@composable
            val mode = backStackEntry.arguments?.getString("mode") ?: "walk"
            val courseId = backStackEntry.arguments?.getString("courseId")?.ifEmpty { null }
            val spotIndex = backStackEntry.arguments?.getString("spotIndex")?.toIntOrNull() ?: 0

            LaunchedEffect(placeId, mode) {
                onNaviStart()
            }

            NaviScreen(
                navController = navController,
                placeId = placeId,
                mode = mode,
                courseId = courseId,
                spotIndex = spotIndex,
                onMinimize = {
                    onNaviMinimize(placeId, mode)
                    navController.popBackStack()
                },
            )
        }
    }
}