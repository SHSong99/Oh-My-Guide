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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ohmyguide.app.R
import com.ohmyguide.app.data.model.UserResponse
import com.ohmyguide.app.domain.model.BookmarkedPhrase
import com.ohmyguide.app.domain.model.PhraseBookmarkStore
import com.ohmyguide.app.ui.common.BottomNavBar
import com.ohmyguide.app.ui.navi.Screen
import com.ohmyguide.app.ui.theme.AppLanguage
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
import com.ohmyguide.app.ui.theme.OhMyGuideTheme
import com.ohmyguide.app.ui.theme.Primary
import com.ohmyguide.app.ui.theme.PrimaryBg
import com.ohmyguide.app.ui.theme.PrimaryBgLight
import com.ohmyguide.app.ui.theme.TextCaption
import com.ohmyguide.app.ui.theme.TextPrimary
import com.ohmyguide.app.ui.theme.TextSecondary

@Composable
fun MyPageScreen(
    navController: NavController,
    viewModel: MyPageViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val strings = LocalStrings.current
    val context = LocalContext.current
    val bookmarkMap by PhraseBookmarkStore.bookmarks.collectAsState()

    var showLanguageDialog by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgWhite),
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

        if (state.isLoading) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator(color = Primary) }
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(BgScreen)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            ) {
                ProfileSection(
                    user = state.user,
                    onEditClick = { showProfileDialog = true },
                )
                Spacer(modifier = Modifier.height(16.dp))
                PickRecommendSection(places = state.pickPlaces, isLoading = state.pickLoading)
                Spacer(modifier = Modifier.height(16.dp))
                BookmarkedPhrasesSection(
                    bookmarks = bookmarkMap.values.toList(),
                    onRemove = { key -> PhraseBookmarkStore.remove(key) },
                    onViewAll = { navController.navigate(Screen.Phrases.route) },
                )
                Spacer(modifier = Modifier.height(16.dp))
                MenuSection(
                    onLanguageClick = { showLanguageDialog = true },
                )
                Spacer(modifier = Modifier.height(16.dp))
                SignOutButton(
                    onClick = {
                        viewModel.logout {
                            navController.navigate(Screen.Welcome.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    },
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        BottomNavBar(
            activeTab = "mypage",
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

    if (showLanguageDialog) {
        LanguagePickerDialog(
            onDismiss = { showLanguageDialog = false },
            onSelect = { lang ->
                LanguageManager.setLanguage(context, lang)
                showLanguageDialog = false
            },
        )
    }

    if (showProfileDialog) {
        ProfileEditDialog(
            user = state.user,
            onDismiss = { showProfileDialog = false },
            onSave = { nationality, age, gender ->
                viewModel.updateProfile(nationality, age, gender)
                showProfileDialog = false
            },
        )
    }
}

// ── Language Picker Dialog ──

@Composable
private fun LanguagePickerDialog(
    onDismiss: () -> Unit,
    onSelect: (AppLanguage) -> Unit,
) {
    val current = LanguageManager.current.value

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(BgWhite)
                .padding(24.dp),
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Filled.Language,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = Primary,
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = LocalStrings.current.language,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Close",
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onDismiss)
                        .padding(4.dp),
                    tint = TextCaption,
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Language options
            val languages = listOf(
                Triple(AppLanguage.EN, "\uD83C\uDDFA\uD83C\uDDF8", "English"),
                Triple(AppLanguage.KO, "\uD83C\uDDF0\uD83C\uDDF7", "\uD55C\uAD6D\uC5B4"),
                Triple(AppLanguage.JA, "\uD83C\uDDEF\uD83C\uDDF5", "\u65E5\u672C\u8A9E"),
                Triple(AppLanguage.ZH_CN, "\uD83C\uDDE8\uD83C\uDDF3", "\u7B80\u4F53\u4E2D\u6587"),
                Triple(AppLanguage.ZH_TW, "\uD83C\uDDF9\uD83C\uDDFC", "\u7E41\u9AD4\u4E2D\u6587"),
            )

            languages.forEach { (lang, flag, label) ->
                val isSelected = lang == current
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) PrimaryBg else BgScreen)
                        .border(
                            width = if (isSelected) 1.5.dp else 0.dp,
                            color = if (isSelected) Primary else BgScreen,
                            shape = RoundedCornerShape(14.dp),
                        )
                        .clickable { onSelect(lang) }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = flag,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        ),
                        color = if (isSelected) Primary else TextPrimary,
                        modifier = Modifier.weight(1f),
                    )
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Primary),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = BgWhite,
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Profile Edit Dialog ──

@Composable
private fun ProfileEditDialog(
    user: UserResponse?,
    onDismiss: () -> Unit,
    onSave: (nationality: String, age: Int, gender: String) -> Unit,
) {
    val strings = LocalStrings.current
    var nationality by remember { mutableStateOf(user?.nationality ?: "") }
    var ageText by remember { mutableStateOf(user?.age?.toString() ?: "") }
    var gender by remember { mutableStateOf(user?.gender ?: "") }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.editProfile) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nationality,
                    onValueChange = { nationality = it; showError = false },
                    label = { Text(strings.nationality) },
                    singleLine = true,
                    isError = showError && nationality.isBlank(),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = ageText,
                    onValueChange = { ageText = it.filter { c -> c.isDigit() }; showError = false },
                    label = { Text(strings.age) },
                    singleLine = true,
                    isError = showError && ageText.isBlank(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = strings.gender,
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("M", "F").forEach { g ->
                        val selected = gender == g
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selected) Primary else if (showError && gender.isBlank()) Error.copy(alpha = 0.1f) else PrimaryBgLight)
                                .clickable { gender = g; showError = false }
                                .padding(horizontal = 20.dp, vertical = 10.dp),
                        ) {
                            Text(
                                text = if (g == "M") "Male" else "Female",
                                color = if (selected) BgWhite else TextPrimary,
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                            )
                        }
                    }
                }
                if (showError) {
                    Text(
                        text = strings.profileFieldsRequired,
                        style = MaterialTheme.typography.labelSmall,
                        color = Error,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val age = ageText.toIntOrNull()
                    if (age == null || nationality.isBlank() || gender.isBlank()) {
                        showError = true
                        return@TextButton
                    }
                    onSave(nationality, age, gender)
                },
            ) { Text(strings.save) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(strings.back) }
        },
    )
}

// ── Profile Section ──

@Composable
private fun ProfileSection(user: UserResponse?, onEditClick: () -> Unit) {
    val strings = LocalStrings.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(BgWhite)
            .border(1.dp, BorderLight, RoundedCornerShape(20.dp))
            .padding(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(R.drawable.face),
                contentDescription = "Profile",
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .border(3.dp, Primary.copy(alpha = 0.3f), CircleShape),
                contentScale = ContentScale.Fit,
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user?.name ?: strings.traveler,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary,
                )
                if (user?.email != null) {
                    Text(text = user.email, style = MaterialTheme.typography.labelMedium, color = TextCaption)
                }
            }
            Icon(
                Icons.Filled.Edit,
                contentDescription = "Edit",
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(PrimaryBgLight)
                    .clickable(onClick = onEditClick)
                    .padding(6.dp),
                tint = Primary,
            )
        }
        if (user != null && (user.nationality != null || user.gender != null || user.age != null)) {
            Spacer(modifier = Modifier.height(14.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                user.nationality?.let { InfoChip(icon = Icons.Filled.TravelExplore, text = it) }
                user.gender?.let { InfoChip(icon = Icons.Filled.Person, text = it) }
                user.age?.let { InfoChip(icon = Icons.Filled.Edit, text = "${it}${strings.ageUnit}") }
            }
        }
    }
}

@Composable
private fun InfoChip(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(PrimaryBgLight)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(13.dp), tint = Primary)
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = text, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold), color = Primary)
    }
}

// ── Bookmarked Phrases Section ──

@Composable
private fun BookmarkedPhrasesSection(
    bookmarks: List<BookmarkedPhrase>,
    onRemove: (String) -> Unit,
    onViewAll: () -> Unit,
) {
    val strings = LocalStrings.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(BgWhite)
            .border(1.dp, BorderLight, RoundedCornerShape(20.dp))
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Bookmark, contentDescription = null, modifier = Modifier.size(20.dp), tint = MenuBookmark)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = strings.bookmarks,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary,
                modifier = Modifier.weight(1f),
            )
            if (bookmarks.isNotEmpty()) {
                Text(
                    text = "${bookmarks.size}",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = Primary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(PrimaryBgLight)
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))

        if (bookmarks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(BgScreen)
                    .clickable(onClick = onViewAll)
                    .padding(20.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = strings.bookmarkEmpty,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextCaption,
                )
            }
        } else {
            bookmarks.forEach { bm ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(BgScreen)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = bm.phrase.kr,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary,
                        )
                        Text(
                            text = bm.phrase.pron,
                            style = MaterialTheme.typography.labelSmall.copy(fontStyle = FontStyle.Italic),
                            color = Primary,
                        )
                        Text(
                            text = bm.phrase.en,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                        )
                    }
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "Remove",
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .clickable { onRemove(bm.key) }
                            .padding(4.dp),
                        tint = TextCaption,
                    )
                }
                if (bm != bookmarks.last()) {
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(PrimaryBgLight)
                    .clickable(onClick = onViewAll)
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = strings.viewAllPhrases,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Primary,
                )
            }
        }
    }
}

// ── Pick Recommend Section ──

@Composable
private fun PickRecommendSection(places: List<PickPlace>, isLoading: Boolean) {
    val strings = LocalStrings.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(BgWhite)
            .border(1.dp, BorderLight, RoundedCornerShape(20.dp))
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.TravelExplore, contentDescription = null, modifier = Modifier.size(20.dp), tint = Primary)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = strings.pickRecommendTitle, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = strings.pickRecommendDesc, style = MaterialTheme.typography.labelSmall, color = TextCaption)
        Spacer(modifier = Modifier.height(12.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary, modifier = Modifier.size(24.dp))
            }
        } else if (places.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(BgScreen).padding(20.dp),
                contentAlignment = Alignment.Center,
            ) { Text(text = strings.pickRecommendEmpty, style = MaterialTheme.typography.bodySmall, color = TextCaption) }
        } else {
            places.forEach { place ->
                PickPlaceRow(place = place)
                if (place != places.last()) Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun PickPlaceRow(place: PickPlace) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(BgScreen).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(PrimaryBgLight), contentAlignment = Alignment.Center) {
            Text(text = "#${place.rank}", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = Primary)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = place.title, style = MaterialTheme.typography.titleSmall, color = TextPrimary, maxLines = 1)
            if (!place.addr.isNullOrBlank()) {
                Text(text = place.addr, style = MaterialTheme.typography.labelSmall, color = TextCaption, maxLines = 1)
            }
        }
        if (place.imageUrl != null) {
            coil.compose.AsyncImage(
                model = place.imageUrl, contentDescription = place.title,
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop,
            )
        }
    }
}

// ── Menu Section ──

@Composable
private fun MenuSection(onLanguageClick: () -> Unit) {
    val currentLangLabel = when (LanguageManager.current.value) {
        AppLanguage.EN -> "English"
        AppLanguage.JA -> "\u65E5\u672C\u8A9E"
        AppLanguage.ZH_TW -> "\u7E41\u9AD4\u4E2D\u6587"
        AppLanguage.ZH_CN -> "\u7B80\u4F53\u4E2D\u6587"
        AppLanguage.KO -> "\uD55C\uAD6D\uC5B4"
    }
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(BgWhite).border(1.dp, BorderLight, RoundedCornerShape(20.dp)),
    ) {
        MenuRow(icon = Icons.Filled.Language, iconColor = MenuLang, iconBg = MenuLangBg, label = LocalStrings.current.language, desc = currentLangLabel, onClick = onLanguageClick)
    }
}

@Composable
private fun MenuRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector, iconColor: androidx.compose.ui.graphics.Color,
    iconBg: androidx.compose.ui.graphics.Color, label: String, desc: String, onClick: () -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(iconBg), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = iconColor)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
            Text(text = desc, style = MaterialTheme.typography.labelSmall, color = TextCaption)
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, modifier = Modifier.size(20.dp), tint = TextCaption)
    }
}

// ── Sign Out ──

@Composable
private fun SignOutButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(BgWhite)
            .border(1.dp, BorderLight, RoundedCornerShape(16.dp)).clickable(onClick = onClick).padding(vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp), tint = Error)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = LocalStrings.current.signOut, style = MaterialTheme.typography.titleSmall, color = Error)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun MyPageScreenPreview() {
    OhMyGuideTheme { MyPageScreen(rememberNavController()) }
}
