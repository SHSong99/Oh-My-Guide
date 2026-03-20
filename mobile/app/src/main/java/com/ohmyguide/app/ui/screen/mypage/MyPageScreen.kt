package com.ohmyguide.app.ui.screen.mypage

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ohmyguide.app.R
import androidx.hilt.navigation.compose.hiltViewModel
import com.ohmyguide.app.ui.navi.Screen
import com.ohmyguide.app.ui.theme.AppLanguage
import com.ohmyguide.app.ui.theme.BgDivider
import com.ohmyguide.app.ui.theme.BgScreen
import com.ohmyguide.app.ui.theme.BgWhite
import com.ohmyguide.app.ui.theme.BorderLight
import com.ohmyguide.app.ui.theme.Error
import com.ohmyguide.app.ui.theme.LanguageManager
import com.ohmyguide.app.ui.theme.LocalStrings
import com.ohmyguide.app.ui.theme.MenuBookmark
import com.ohmyguide.app.ui.theme.MenuBookmarkBg
import com.ohmyguide.app.ui.theme.MenuLang
import com.ohmyguide.app.ui.theme.MenuLangBg
import com.ohmyguide.app.ui.theme.MenuNoti
import com.ohmyguide.app.ui.theme.MenuNotiBg
import com.ohmyguide.app.ui.theme.MenuStory
import com.ohmyguide.app.ui.theme.MenuStoryBg
import com.ohmyguide.app.ui.theme.MenuTheme
import com.ohmyguide.app.ui.theme.MenuThemeBg
import com.ohmyguide.app.ui.theme.OhMyGuideTheme
import com.ohmyguide.app.ui.theme.Primary
import com.ohmyguide.app.ui.theme.PrimaryBg
import com.ohmyguide.app.ui.theme.PrimaryBgLight
import com.ohmyguide.app.ui.theme.TextCaption
import com.ohmyguide.app.ui.theme.TextPrimary

private data class MenuItem(
    val icon: ImageVector,
    val color: Color,
    val bgColor: Color,
    val label: String,
    val desc: String,
    val count: Int? = null,
)

@Composable
private fun menuItems(): List<MenuItem> {
    val strings = LocalStrings.current
    val currentLangLabel = when (LanguageManager.current.value) {
        AppLanguage.EN -> "English"
        AppLanguage.JA -> "\u65E5\u672C\u8A9E"
        AppLanguage.ZH_TW -> "\u7E41\u9AD4\u4E2D\u6587"
        AppLanguage.ZH_CN -> "\u7B80\u4F53\u4E2D\u6587"
        AppLanguage.KO -> "\uD55C\uAD6D\uC5B4"
    }
    return listOf(
        MenuItem(Icons.Filled.LocationOn, Primary, PrimaryBg, strings.visitHistory, strings.placesVisitedDesc, count = 0),
        MenuItem(Icons.Filled.Bookmark, MenuBookmark, MenuBookmarkBg, strings.bookmarks, strings.savedPlacesDesc, count = 0),
        MenuItem(Icons.Filled.Headphones, MenuStory, MenuStoryBg, strings.storyArchive, strings.storiesListenedDesc, count = 0),
        MenuItem(Icons.Filled.Language, MenuLang, MenuLangBg, strings.language, currentLangLabel),
        MenuItem(Icons.Filled.Notifications, MenuNoti, MenuNotiBg, strings.notifications, strings.on),
        MenuItem(Icons.Filled.Palette, MenuTheme, MenuThemeBg, strings.theme, strings.light),
    )
}

sealed class MyPageUiState {
    object Loading : MyPageUiState()
    object Idle : MyPageUiState()
    data class Error(val message: String) : MyPageUiState()
}

@Composable
fun MyPageScreen(navController: NavController) {
    val viewModel: MyPageViewModel = hiltViewModel()
    val strings = LocalStrings.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgScreen),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BgWhite)
                .padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            Text(
                text = strings.myPage,
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(BorderLight),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            ProfileCard()
            Spacer(modifier = Modifier.height(16.dp))
            MenuCard()
            Spacer(modifier = Modifier.height(16.dp))
            SignOutButton(onClick = {
                viewModel.logout {
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            })
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ProfileCard() {
    val strings = LocalStrings.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(BgWhite)
            .border(1.dp, BorderLight, RoundedCornerShape(20.dp))
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(R.drawable.masot),
            contentDescription = "Profile",
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .border(3.dp, Primary, CircleShape)
                .background(PrimaryBg),
            contentScale = ContentScale.Crop,
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = strings.traveler,
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Seoul, Jongno area",
                style = MaterialTheme.typography.bodySmall,
                color = TextCaption,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                InterestTag(icon = Icons.Filled.Restaurant, color = Primary, label = strings.food)
                InterestTag(icon = Icons.Filled.AccountBalance, color = Primary, label = strings.culture)
            }
        }
    }
}

@Composable
private fun InterestTag(icon: ImageVector, color: Color, label: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(PrimaryBgLight)
            .padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(12.dp), tint = color)
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = Primary,
        )
    }
}

@Composable
private fun MenuCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(BgWhite)
            .border(1.dp, BorderLight, RoundedCornerShape(20.dp)),
    ) {
        menuItems().forEachIndexed { index, item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(item.bgColor),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(item.icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = item.color)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                    )
                    Text(
                        text = item.desc,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextCaption,
                    )
                }
                if (item.count != null) {
                    Text(
                        text = "${item.count}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = Primary,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, modifier = Modifier.size(20.dp), tint = TextCaption)
            }
            if (index < menuItems().size - 1) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(1.dp)
                        .background(BgDivider),
                )
            }
        }
    }
}

@Composable
private fun SignOutButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BgWhite)
            .border(1.dp, BorderLight, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp), tint = Error)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = LocalStrings.current.signOut,
                style = MaterialTheme.typography.titleSmall,
                color = Error,
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun MyPageScreenPreview() {
    OhMyGuideTheme {
        MyPageScreen(rememberNavController())
    }
}
