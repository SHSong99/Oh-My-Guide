package com.ohmyguide.app.ui.screen.explore

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ohmyguide.app.fixtures.Course
import com.ohmyguide.app.fixtures.EXPLORE_CATEGORY_GROUPS
import com.ohmyguide.app.fixtures.EXPLORE_COURSES
import com.ohmyguide.app.ui.theme.BgSub
import com.ohmyguide.app.ui.theme.BgWhite
import com.ohmyguide.app.ui.theme.OhMyGuideTheme
import com.ohmyguide.app.ui.theme.Star
import com.ohmyguide.app.ui.theme.TextCaption
import com.ohmyguide.app.ui.theme.TextPrimary
import com.ohmyguide.app.ui.theme.TextSecondary

@Composable
fun CourseCard(
    course: Course,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    featured: Boolean = false,
) {
    val catGroup = EXPLORE_CATEGORY_GROUPS.find { it.key == course.category }
    val catColor = catGroup?.color ?: TextCaption
    val height = if (featured) 200.dp else 160.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(catColor.copy(alpha = 0.15f), catColor.copy(alpha = 0.35f))
                )
            )
            .clickable(onClick = onClick),
    ) {
        // Emoji background
        Text(
            text = course.emoji,
            fontSize = 64.sp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            color = Color.White.copy(alpha = 0.15f),
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .padding(16.dp),
        ) {
            // Category badge
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(catColor.copy(alpha = 0.9f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = catGroup?.emoji ?: "", fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = catGroup?.label ?: "",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = BgWhite,
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                // Tags
                course.tags.take(2).forEach { tag ->
                    Text(
                        text = "#$tag",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextPrimary.copy(alpha = 0.6f),
                        modifier = Modifier.padding(end = 6.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                text = course.title,
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = course.subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = TextCaption,
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Meta row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MetaPill(icon = Icons.Filled.LocationOn, text = "${course.spotCount} spots")
                    MetaPill(icon = Icons.Filled.AccessTime, text = course.duration)
                    MetaPill(icon = Icons.Filled.Star, text = "${course.rating}")
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(TextPrimary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp), tint = TextPrimary)
                }
            }
        }
    }
}

@Composable
private fun MetaPill(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(BgWhite.copy(alpha = 0.6f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(12.dp), tint = TextPrimary)
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = TextPrimary,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CourseCardPreview() {
    OhMyGuideTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            CourseCard(course = EXPLORE_COURSES[0], onClick = {}, featured = true)
            Spacer(modifier = Modifier.height(16.dp))
            CourseCard(course = EXPLORE_COURSES[1], onClick = {})
        }
    }
}