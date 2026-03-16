package com.ohmyguide.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.ohmyguide.app.ui.navi.NavGraph
import com.ohmyguide.app.ui.theme.OhMyGuideTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OhMyGuideTheme {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}
