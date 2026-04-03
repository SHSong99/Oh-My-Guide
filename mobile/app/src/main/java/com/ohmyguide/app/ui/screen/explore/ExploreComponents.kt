package com.ohmyguide.app.ui.screen.explore

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ohmyguide.app.R
import com.ohmyguide.app.fixtures.EXPLORE_CATEGORY_GROUPS
import com.ohmyguide.app.fixtures.EXPLORE_REGIONS
import com.ohmyguide.app.fixtures.Region
import com.ohmyguide.app.ui.theme.BgWhite
import com.ohmyguide.app.ui.theme.Border
import com.ohmyguide.app.ui.theme.ExploreDarkBg
import com.ohmyguide.app.ui.theme.HotBadge
import com.ohmyguide.app.ui.theme.LocalStrings
import com.ohmyguide.app.ui.theme.Primary
import com.ohmyguide.app.ui.theme.PrimaryBg
import com.ohmyguide.app.ui.theme.TextCaption
import com.ohmyguide.app.ui.theme.TextPrimary
import com.ohmyguide.app.ui.theme.TextSecondary

// ── Hero Banner ──

@Composable
fun HeroBanner() {
    val strings = LocalStrings.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .background(ExploreDarkBg),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            ExploreDarkBg.copy(alpha = 0.5f),
                            ExploreDarkBg.copy(alpha = 0.2f),
                            ExploreDarkBg,
                        ),
                    )
                ),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(R.drawable.face),
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                contentScale = ContentScale.Fit,
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = strings.kCulture,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = BgWhite,
                )
                Text(
                    text = strings.explore,
                    style = MaterialTheme.typography.labelSmall,
                    color = BgWhite.copy(alpha = 0.6f),
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 20.dp, vertical = 20.dp),
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(HotBadge.copy(alpha = 0.8f))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Whatshot,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = BgWhite,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = strings.trending,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = BgWhite,
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = strings.diveIntoKCulture,
                style = MaterialTheme.typography.displayLarge,
                color = BgWhite,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = strings.curatedCourses,
                style = MaterialTheme.typography.bodySmall,
                color = BgWhite.copy(alpha = 0.7f),
            )
        }
    }
}

// ── Existing Components ──

@Composable
fun CategoryCardsRow(
    selectedCategory: String?,
    onCategoryClick: (String) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .background(ExploreDarkBg)
            .padding(vertical = 8.dp),
    ) {
        items(EXPLORE_CATEGORY_GROUPS) { cg ->
            val active = selectedCategory == cg.key
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(72.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        if (active) cg.color.copy(alpha = 0.7f)
                        else Color.White.copy(alpha = 0.1f)
                    )
                    .then(
                        if (active) Modifier.border(2.dp, cg.color, RoundedCornerShape(18.dp))
                        else Modifier
                    )
                    .clickable { onCategoryClick(cg.key) },
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(
                                if (active) BgWhite.copy(alpha = 0.3f)
                                else BgWhite.copy(alpha = 0.15f)
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            cg.icon,
                            contentDescription = cg.label,
                            modifier = Modifier.size(16.dp),
                            tint = BgWhite,
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = cg.label,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = BgWhite,
                    )
                }
            }
        }
    }
}

@Composable
fun RegionChips(
    regions: List<Region> = EXPLORE_REGIONS,
    selectedRegion: String,
    onRegionClick: (String) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        items(regions) { region ->
            val active = selectedRegion == region.id
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (active) TextPrimary else BgWhite)
                    .then(
                        if (active) Modifier else Modifier.border(1.dp, Border, RoundedCornerShape(20.dp))
                    )
                    .clickable { onRegionClick(region.id) }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            ) {
                Text(
                    text = region.name,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = if (active) BgWhite else TextSecondary,
                )
            }
        }
    }
}

@Composable
fun SectionHeader(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    iconBg: Color,
    count: Int? = null,
) {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = title, modifier = Modifier.size(15.dp), tint = iconTint)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
            color = TextPrimary,
        )
        if (count != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$count ${LocalStrings.current.courses.lowercase()}",
                style = MaterialTheme.typography.labelSmall,
                color = TextCaption,
            )
        }
    }
}

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(PrimaryBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Search, contentDescription = "Search", modifier = Modifier.size(28.dp), tint = Primary)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = LocalStrings.current.noCoursesFound,
            style = MaterialTheme.typography.titleSmall,
            color = TextCaption,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = LocalStrings.current.tryDifferentRegion,
            style = MaterialTheme.typography.labelMedium,
            color = TextCaption.copy(alpha = 0.7f),
        )
    }
}
