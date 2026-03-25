package com.ohmyguide.app.ui.screen.phrases

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ohmyguide.app.domain.model.PhraseBookmarkStore
import com.ohmyguide.app.fixtures.PHRASE_SECTIONS
import com.ohmyguide.app.ui.common.BottomNavBar
import com.ohmyguide.app.ui.navi.Screen
import com.ohmyguide.app.ui.theme.BgScreen
import com.ohmyguide.app.ui.theme.BgWhite
import com.ohmyguide.app.ui.theme.OhMyGuideTheme

sealed class PhrasesUiState {
    object Loading : PhrasesUiState()
    object Idle : PhrasesUiState()
    data class Error(val message: String) : PhrasesUiState()
}

@Composable
fun PhrasesScreen(navController: NavController) {
    var expandedSection by remember { mutableStateOf<String?>(null) }
    val bookmarkMap by PhraseBookmarkStore.bookmarks.collectAsState()
    val savedPhrases = bookmarkMap.keys

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgWhite),
    ) {
        PhrasesHeader(savedCount = savedPhrases.size)

        MascotTip()

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .background(BgScreen)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            itemsIndexed(PHRASE_SECTIONS) { _, section ->
                val isOpen = expandedSection == section.title || expandedSection == null
                PhraseSectionCard(
                    section = section,
                    isOpen = isOpen,
                    savedPhrases = savedPhrases,
                    onToggle = {
                        expandedSection = if (expandedSection == section.title) null else section.title
                    },
                    onSaveToggle = { key, phrase ->
                        PhraseBookmarkStore.toggle(key, phrase, section.title)
                    },
                )
            }
        }

        BottomNavBar(
            activeTab = "phrases",
            onTabChange = { tab ->
                when (tab) {
                    "main" -> navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                        launchSingleTop = true
                        restoreState = true
                    }
                    "explore" -> navController.navigate(Screen.Explore.route) {
                        popUpTo(Screen.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PhrasesScreenPreview() {
    OhMyGuideTheme {
        PhrasesScreen(rememberNavController())
    }
}
