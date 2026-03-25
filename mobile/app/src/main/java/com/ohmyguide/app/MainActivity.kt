package com.ohmyguide.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.ohmyguide.app.ui.common.FloatingNavButton
import com.ohmyguide.app.ui.common.NavMinimizedState
import com.ohmyguide.app.ui.navi.NavGraph
import com.ohmyguide.app.ui.navi.Screen
import com.ohmyguide.app.ui.theme.LanguageManager
import com.ohmyguide.app.ui.theme.LocalStrings
import com.ohmyguide.app.ui.theme.OhMyGuideTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LanguageManager.init(this)
        enableEdgeToEdge()
        setContent {
            val language by LanguageManager.current

            CompositionLocalProvider(LocalStrings provides language.strings) {
                OhMyGuideTheme {
                    val navController = rememberNavController()
                    val navMinimizedState = remember { NavMinimizedState() }

                    Box(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
                        NavGraph(
                            navController = navController,
                            onNaviMinimize = { placeId, mode ->
                                navMinimizedState.minimize(placeId, mode)
                            },
                            onNaviStart = {
                                navMinimizedState.stop()
                            },
                        )

                        if (navMinimizedState.isMinimized) {
                            FloatingNavButton(
                                onRestore = {
                                    val placeId = navMinimizedState.placeId ?: return@FloatingNavButton
                                    val mode = navMinimizedState.mode
                                    navMinimizedState.restore()
                                    navController.navigate(Screen.Navi.createRoute(placeId, mode))
                                },
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(end = 20.dp, bottom = 80.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}