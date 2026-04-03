package com.ohmyguide.app.fixtures

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.ohmyguide.app.ui.theme.CatAttraction
import com.ohmyguide.app.ui.theme.CatCafe
import com.ohmyguide.app.ui.theme.CatCourse
import com.ohmyguide.app.ui.theme.CatCulture
import com.ohmyguide.app.ui.theme.CatFestival
import com.ohmyguide.app.ui.theme.CatFood
import com.ohmyguide.app.ui.theme.CatLeports
import com.ohmyguide.app.ui.theme.CatShopping
import com.ohmyguide.app.ui.theme.Success
import androidx.annotation.RawRes
import com.ohmyguide.app.R

// ── Featured Theme ──

data class FeaturedTheme(
    val id: String,
    val title: String,
    val subtitle: String,
    val badge: String,
    val courseId: Long,
    @RawRes val videoRes: Int,
    val dominantColor: Long = 0xFF1A1A2E,
)

val FEATURED_THEMES = listOf(
    FeaturedTheme(
        id = "kpop-demon",
        title = "K-Pop 데몬 헌터스",
        subtitle = "케데헌 속 한국 명소를 함께 걸어 보세요",
        badge = "K-POP",
        courseId = 1,
        videoRes = R.raw.theme_kpop_demon_hunters,
        dominantColor = 0xFF8B1A1A,
    ),
    FeaturedTheme(
        id = "bts",
        title = "BTS ARMY\nBusan Tour",
        subtitle = "Walk the streets where BTS made history",
        badge = "BTS",
        courseId = 4,
        videoRes = R.raw.theme_bts,
        dominantColor = 0xFF1A2B5E,
    ),
    FeaturedTheme(
        id = "ssafy",
        title = "SSAFY 점심\n산책 코스",
        subtitle = "SSAFY 부울경캠퍼스 주변 점심시간 활용 산책 루트",
        badge = "SSAFY",
        courseId = 6,
        videoRes = R.raw.theme_ssafy,
        dominantColor = 0xFF1A4B8E,
    ),
)

// ── Category ──

data class Category(
    val id: String,
    val emoji: String,
    val name: String,
    val sub: String,
    val color: Color,
)

val CATEGORIES = listOf(
    Category("attraction", "\uD83C\uDFDE\uFE0F", "Attraction", "Landmarks & nature", CatAttraction),
    Category("culture", "\uD83C\uDFDB\uFE0F", "Culture", "Museums & history", CatCulture),
    Category("festival", "\uD83C\uDF86", "Festival", "Events & performances", CatFestival),
    Category("course", "\uD83D\uDDFA\uFE0F", "Course", "Travel routes", CatCourse),
    Category("leports", "\uD83C\uDFC4\u200D\u2642\uFE0F", "Leports", "Leisure & sports", CatLeports),
    Category("cafe", "\u2615", "Cafes", "Coffee & bakeries", CatCafe),
    Category("shopping", "\uD83D\uDECD\uFE0F", "Shopping", "Markets & malls", CatShopping),
    Category("food", "\uD83C\uDF5C", "Food", "Dining & street eats", CatFood),
)

// ── Place ──

data class Place(
    val id: String,
    val name: String,
    val nameKr: String,
    val rating: Float,
    val distance: String,
    val tag: String,
    val color: Color,
    val emoji: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val imageUrl: String? = null,
)

data class PlaceDetail(
    val place: Place,
    val desc: String,
    val hours: String,
    val fee: String,
    val walkTime: String,
)

val SAMPLE_PLACES = listOf(
    Place("dm3", "Sinho Beach Trail", "\uC2E0\uD638\uB3D9 \uD574\uC548\uC0B0\uCC45\uB85C", 4.7f, "2.5km", "Nature", CatAttraction, "\uD83C\uDF0A", 35.0807, 128.8785, "https://images.unsplash.com/photo-1517154421773-0529f29ea451?q=80&w=600&auto=format&fit=crop"),
    Place("dm4", "Nakdong Estuary Eco Center", "\uB099\uB3D9\uAC15\uD558\uAD6C\uC5D0\uCF54\uC13C\uD130", 4.8f, "8.5km", "Nature", CatAttraction, "\uD83E\uDEB6", 35.1044, 128.9459, "https://images.unsplash.com/photo-1588668214407-6ea9a6d8c272?q=80&w=600&auto=format&fit=crop"),
    Place("dm5", "Gimhae International Airport", "\uAE40\uD574\uAD6D\uC81C\uACF5\uD56D", 4.6f, "10km", "Culture", CatCulture, "\u2708\uFE0F", 35.1795, 128.9383, "https://images.unsplash.com/photo-1698210876771-36cb7af6e852?q=80&w=600&auto=format&fit=crop"),
    Place("dm6", "Daejeo Eco Park", "\uB300\uC800\uC0DD\uD0DC\uACF5\uC6D0", 4.9f, "9.5km", "Nature", CatAttraction, "\uD83C\uDF38", 35.2110, 128.9722, "https://images.unsplash.com/photo-1704240699154-da9e9c690373?q=80&w=600&auto=format&fit=crop"),
    Place("dm7", "Jangnim Port", "\uC7A5\uB9BC\uD3EC\uAD6C", 4.5f, "7.0km", "Food", CatFood, "\uD83E\uDD90", 35.0720, 128.9650, "https://images.unsplash.com/photo-1540138279543-b3728f037467?q=80&w=600&auto=format&fit=crop"),
)

val SAMPLE_PLACE_DETAILS = mapOf(
    "dm3" to PlaceDetail(
        place = SAMPLE_PLACES[0],
        desc = "A peaceful coastal trail in Sinho-dong where the Nakdong River meets the sea. Walk along the black pine forest with ocean views.",
        hours = "Open 24h",
        fee = "Free",
        walkTime = "30 min walk",
    ),
    "dm4" to PlaceDetail(
        place = SAMPLE_PLACES[1],
        desc = "A world-renowned migratory bird sanctuary at the Nakdong River estuary. Features wetland exhibitions and wildlife observation decks.",
        hours = "09:00 - 18:00",
        fee = "Free",
        walkTime = "105 min walk",
    ),
    "dm5" to PlaceDetail(
        place = SAMPLE_PLACES[2],
        desc = "Busan's international airport with an observation deck to watch planes take off. Great spot for aviation fans.",
        hours = "06:00 - 22:00",
        fee = "Free",
        walkTime = "130 min walk",
    ),
    "dm6" to PlaceDetail(
        place = SAMPLE_PLACES[3],
        desc = "A 182-hectare riverside park famous for canola flowers in spring and cherry blossoms. Hosts the annual Nakdong River Canola Festival.",
        hours = "06:00 - 21:00",
        fee = "Free",
        walkTime = "150 min walk",
    ),
    "dm7" to PlaceDetail(
        place = SAMPLE_PLACES[4],
        desc = "A traditional fishing port known for fresh seafood restaurants. Try the famous raw fish and crab dishes at the waterfront market.",
        hours = "06:00 - 22:00",
        fee = "Free",
        walkTime = "90 min walk",
    ),
    "p3" to PlaceDetail(
        place = Place("p3", "Jinudo Island", "\uC9C4\uC6B0\uB3C4", 4.7f, "6.0km", "Nature", CatAttraction, lat = 35.0850, lng = 128.9200, imageUrl = "https://images.unsplash.com/photo-1682090369590-c4c82f3cc065?q=80&w=600&auto=format&fit=crop"),
        desc = "A sandy delta island where the river meets the ocean. Accessible by ferry, offering pristine beaches and tidal flats.",
        hours = "Open 24h",
        fee = "Free",
        walkTime = "75 min walk",
    ),
    "p4" to PlaceDetail(
        place = Place("p4", "Amisan Observatory", "\uC544\uBBF8\uC0B0\uC804\uB9DD\uB300", 4.8f, "8.0km", "Nature", CatAttraction, lat = 35.0530, lng = 128.9580, imageUrl = "https://images.unsplash.com/photo-1768006273763-85c9ff25e5fa?q=80&w=600&auto=format&fit=crop"),
        desc = "A scenic hilltop observatory in Dadaepo offering panoramic views of the Nakdong River estuary and the South Sea.",
        hours = "Open 24h",
        fee = "Free",
        walkTime = "100 min walk",
    ),
    "p5" to PlaceDetail(
        place = Place("p5", "Dadaepo Beach", "\uB2E4\uB300\uD3EC\uD574\uC218\uC695\uC7A5", 4.6f, "9.5km", "Nature", CatAttraction, lat = 35.0470, lng = 128.9660, imageUrl = "https://images.unsplash.com/photo-1617577367443-2d778fedeef4?q=80&w=600&auto=format&fit=crop"),
        desc = "A beautiful beach famous for its stunning sunset fountain show. The Dadaepo Sunset Fountain of Dreams is one of Korea's largest.",
        hours = "Open 24h",
        fee = "Free",
        walkTime = "120 min walk",
    ),
    // ── Course Spots ──
    "dh1" to PlaceDetail(
        place = Place("dh1", "HYBE Insight", "\uD558\uC774\uBE0C \uC778\uC0AC\uC774\uD2B8", 4.9f, "0km", "K-Pop", CatCulture, "\uD83C\uDFB5", 37.5265, 127.0405, "https://images.unsplash.com/photo-1713816821469-6af8114275c5?q=80&w=600&auto=format&fit=crop"),
        desc = "The official museum of HYBE entertainment. Explore interactive exhibits about BTS, TXT, and more.",
        hours = "10:00 - 19:00",
        fee = "22,000 KRW",
        walkTime = "Start point",
    ),
    "dh2" to PlaceDetail(
        place = Place("dh2", "Hongdae Busking Stage", "\uD64D\uB300 \uBC84\uC2A4\uD0B9 \uBB34\uB300", 4.7f, "1.2km", "K-Pop", CatCulture, "\uD83C\uDFA4", 37.5563, 126.9236, "https://images.unsplash.com/photo-1765375783706-05aeeaf59e5f?q=80&w=600&auto=format&fit=crop"),
        desc = "The legendary busking area where K-pop idols were first discovered. Watch street performances every weekend.",
        hours = "Open 24h",
        fee = "Free",
        walkTime = "15 min walk",
    ),
    "dh3" to PlaceDetail(
        place = Place("dh3", "SM Entertainment Cafe", "SM \uC5D4\uD130 \uCE74\uD398", 4.6f, "0.8km", "K-Pop", CatCulture, "\u2615", 37.5586, 126.9267, "https://images.unsplash.com/photo-1603685568162-67024e818bec?q=80&w=600&auto=format&fit=crop"),
        desc = "Official SM cafe with themed drinks and exclusive merchandise. A must-visit for K-pop fans.",
        hours = "10:00 - 22:00",
        fee = "Free entry",
        walkTime = "10 min walk",
    ),
)

// ── Korean Phrases ──

data class KoreanPhrase(
    val kr: String,
    val pron: String,
    val en: String,
)

data class PhraseSection(
    val title: String,
    val subtitle: String,
    val emoji: String,
    val color: Color,
    val phrases: List<KoreanPhrase>,
)

val PHRASE_SECTIONS = listOf(
    PhraseSection(
        title = "Basic Expressions",
        subtitle = "Essential phrases",
        emoji = "\uD83D\uDC4B",
        color = CatCulture,
        phrases = listOf(
            KoreanPhrase("\uC548\uB155\uD558\uC138\uC694", "an-nyeong-ha-se-yo", "Hello"),
            KoreanPhrase("\uAC10\uC0AC\uD569\uB2C8\uB2E4", "gam-sa-ham-ni-da", "Thank you"),
            KoreanPhrase("\uC8C4\uC1A1\uD569\uB2C8\uB2E4", "joe-song-ham-ni-da", "I'm sorry"),
        ),
    ),
    PhraseSection(
        title = "At a Restaurant",
        subtitle = "Ordering food",
        emoji = "\uD83C\uDF5C",
        color = CatFood,
        phrases = listOf(
            KoreanPhrase("\uC774\uAC70 \uC8FC\uC138\uC694", "i-geo ju-se-yo", "This one, please"),
            KoreanPhrase("\uC5BC\uB9C8\uC608\uC694?", "eol-ma-ye-yo", "How much is it?"),
            KoreanPhrase("\uB9DB\uC788\uC5B4\uC694!", "ma-si-sseo-yo", "It's delicious!"),
        ),
    ),
    PhraseSection(
        title = "Getting Around",
        subtitle = "Navigation help",
        emoji = "\uD83D\uDEB6",
        color = CatCourse,
        phrases = listOf(
            KoreanPhrase("\uC5EC\uAE30 \uC5B4\uB514\uC608\uC694?", "yeo-gi eo-di-ye-yo", "Where is this?"),
            KoreanPhrase("\uC9C0\uD558\uCCA0\uC5ED \uC5B4\uB514\uC608\uC694?", "ji-ha-cheol-yeok eo-di-ye-yo", "Where is the subway station?"),
        ),
    ),
    PhraseSection(
        title = "While Shopping",
        subtitle = "Market & store phrases",
        emoji = "\uD83D\uDECD\uFE0F",
        color = CatShopping,
        phrases = listOf(
            KoreanPhrase("\uAE4C\uAE4C\uC8FC\uC138\uC694", "kka-kka-ju-se-yo", "Please give a discount"),
            KoreanPhrase("\uCE74\uB4DC \uB3FC\uC694?", "ka-deu dwae-yo", "Do you accept cards?"),
        ),
    ),
    PhraseSection(
        title = "Emergency",
        subtitle = "When you need help",
        emoji = "\uD83D\uDEA8",
        color = CatFestival,
        phrases = listOf(
            KoreanPhrase("\uB3C4\uC640\uC8FC\uC138\uC694!", "do-wa-ju-se-yo", "Help me!"),
            KoreanPhrase("\uACBD\uCC30 \uBD88\uB7EC\uC8FC\uC138\uC694", "gyeong-chal bul-leo-ju-se-yo", "Please call the police"),
        ),
    ),
)

// ── Onboarding (GpsPermissionScreen) ──

data class LanguageOption(val id: String, val label: String, val flag: String, val available: Boolean = true)
data class CompanionOption(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val iconColor: Color,
    val bgColor: Color,
)
data class CountryOption(val id: String, val flag: String, val name: String)

val LANGUAGE_OPTIONS = listOf(
    LanguageOption("en", "English", "\uD83C\uDDFA\uD83C\uDDF8"),
    LanguageOption("ko", "\uD55C\uAD6D\uC5B4", "\uD83C\uDDF0\uD83C\uDDF7"),
    LanguageOption("ja", "\u65E5\u672C\u8A9E", "\uD83C\uDDEF\uD83C\uDDF5"),
    LanguageOption("zh-CN", "\u7B80\u4F53\u4E2D\u6587", "\uD83C\uDDE8\uD83C\uDDF3"),
    LanguageOption("zh-TW", "\u7E41\u9AD4\u4E2D\u6587", "\uD83C\uDDF9\uD83C\uDDFC"),
)

data class GenderOption(val id: String, val label: String)
val GENDER_OPTIONS = listOf(
    GenderOption("Female", "Female"),
    GenderOption("Male", "Male"),
)

val COMPANION_OPTIONS = listOf(
    CompanionOption("friends", "Friends", Icons.Filled.Groups, Color(0xFF5478FF), Color(0xFFEEF2FF)),
    CompanionOption("family", "Family", Icons.Filled.FamilyRestroom, Color(0xFF16A34A), Color(0xFFF0FDF4)),
    CompanionOption("solo", "Solo", Icons.Filled.Luggage, Color(0xFFEA580C), Color(0xFFFFF7ED)),
    CompanionOption("partner", "Partner", Icons.Filled.Favorite, Color(0xFFE11D48), Color(0xFFFFF1F2)),
)

val COUNTRY_OPTIONS = listOf(
    CountryOption("us", "\uD83C\uDDFA\uD83C\uDDF8", "USA"),
    CountryOption("jp", "\uD83C\uDDEF\uD83C\uDDF5", "Japan"),
    CountryOption("cn", "\uD83C\uDDE8\uD83C\uDDF3", "China"),
    CountryOption("tw", "\uD83C\uDDF9\uD83C\uDDFC", "Taiwan"),
    CountryOption("gb", "\uD83C\uDDEC\uD83C\uDDE7", "UK"),
    CountryOption("fr", "\uD83C\uDDEB\uD83C\uDDF7", "France"),
    CountryOption("de", "\uD83C\uDDE9\uD83C\uDDEA", "Germany"),
    CountryOption("au", "\uD83C\uDDE6\uD83C\uDDFA", "Australia"),
    CountryOption("ca", "\uD83C\uDDE8\uD83C\uDDE6", "Canada"),
    CountryOption("sg", "\uD83C\uDDF8\uD83C\uDDEC", "Singapore"),
    CountryOption("th", "\uD83C\uDDF9\uD83C\uDDED", "Thailand"),
    CountryOption("vn", "\uD83C\uDDFB\uD83C\uDDF3", "Vietnam"),
    CountryOption("kr", "\uD83C\uDDF0\uD83C\uDDF7", "South Korea"),
    CountryOption("other", "\uD83C\uDF0D", "Other"),
)

// ── Welcome Screen Features ──

data class FeatureItem(
    val emoji: String,
    val label: String,
)

val WELCOME_FEATURES = listOf(
    FeatureItem("\uD83D\uDCCD", "GPS Guide"),
    FeatureItem("\uD83D\uDDE3\uFE0F", "Korean Phrases"),
    FeatureItem("\uD83C\uDFAF", "Personalized"),
    FeatureItem("\uD83D\uDDFA\uFE0F", "Navigation"),
)

// ── Home Recommendations ──

data class RecommendationSection(
    val title: String,
    val icon: ImageVector,
    val label: String,
    val places: List<Place>,
    val btnText: String,
)

// ── Explore: Courses ──

data class Spot(
    val id: String,
    val name: String,
    val nameKr: String,
    val desc: String,
    val walkMin: Int,
    val imageUrl: String? = null,
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val overviewTts: String? = null,
)

data class Course(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: String,
    val region: String,
    val emoji: String,
    val duration: String,
    val spotCount: Int,
    val rating: Float,
    val tags: List<String>,
    val spots: List<Spot>,
    val imageUrl: String? = null,
)

data class Region(val id: String, val name: String)

data class ExploreCategoryGroup(
    val key: String,
    val label: String,
    val sub: String,
    val emoji: String,
    val icon: ImageVector,
    val color: Color,
    val bgColor: Color,
)

val EXPLORE_REGIONS = listOf(
    Region("all", "All"),
    Region("seoul", "Seoul"),
    Region("busan", "Busan"),
    Region("daejeon", "Daejeon"),
    Region("jeju", "Jeju"),
    Region("gyeongju", "Gyeongju"),
)

val EXPLORE_CATEGORY_GROUPS = listOf(
    ExploreCategoryGroup("local", "Local Life", "Markets & food", "\uD83C\uDFEA", Icons.Filled.Luggage, Success, Color(0xFFF0FDF4)),
    ExploreCategoryGroup("kpop", "K-Pop", "Idol hotspots", "\uD83C\uDFB5", Icons.Filled.AutoAwesome, Color(0xFF7C3AED), Color(0xFFF5F3FF)),
    ExploreCategoryGroup("kdrama", "K-Drama", "Filming spots", "\uD83C\uDFAC", Icons.Filled.Favorite, Color(0xFFEA580C), Color(0xFFFFF7ED)),
    ExploreCategoryGroup("culture", "Culture", "Heritage & history", "\uD83C\uDFDB\uFE0F", Icons.Filled.AccountBalance, Color(0xFF0891B2), Color(0xFFECFEFF)),
)

val EXPLORE_COURSES = listOf(
    Course(
        id = "demon-hunters",
        title = "K-Pop Demon Hunters Course",
        subtitle = "Follow the hottest idol spots in Hongdae & Mapo",
        category = "kpop",
        region = "seoul",
        emoji = "\uD83C\uDFB5",
        duration = "3-4h",
        spotCount = 5,
        rating = 4.8f,
        tags = listOf("K-Pop", "Hongdae", "Idol"),
        spots = listOf(
            Spot("dh1", "HYBE Insight", "\uD558\uC774\uBE0C \uC778\uC0AC\uC774\uD2B8", "The official museum of HYBE entertainment.", 0, "https://images.unsplash.com/photo-1713816821469-6af8114275c5?q=80&w=600&auto=format&fit=crop", 37.5265, 127.0405),
            Spot("dh2", "Hongdae Busking Stage", "\uD64D\uB300 \uBC84\uC2A4\uD0B9 \uBB34\uB300", "The legendary busking area.", 15, "https://images.unsplash.com/photo-1765375783706-05aeeaf59e5f?q=80&w=600&auto=format&fit=crop", 37.5563, 126.9236),
            Spot("dh3", "SM Entertainment Caf\u00E9", "SM \uC5D4\uD130 \uCE74\uD398", "Official SM caf\u00E9 with themed drinks.", 10, "https://images.unsplash.com/photo-1603685568162-67024e818bec?q=80&w=600&auto=format&fit=crop", 37.5586, 126.9267),
        ),
        imageUrl = "https://images.unsplash.com/photo-1765375783706-05aeeaf59e5f?q=80&w=600&auto=format&fit=crop",
    ),
    Course(
        id = "cvs-mukbang",
        title = "Convenience Store Mukbang",
        subtitle = "Eat like a local \u2014 the ultimate K-CVS food tour",
        category = "local",
        region = "seoul",
        emoji = "\uD83C\uDF5C",
        duration = "2-3h",
        spotCount = 4,
        rating = 4.6f,
        tags = listOf("Local", "Food", "Mukbang"),
        spots = listOf(
            Spot("cvs1", "CU Flagship Seongsu", "CU \uC131\uC218 \uD50C\uB798\uADF8\uC2ED", "Korea's trendiest CU store.", 0, "https://images.unsplash.com/photo-1760020890915-ca605575b93b?q=80&w=600&auto=format&fit=crop", 37.5446, 127.0566),
            Spot("cvs2", "GS25 Hangang Park", "GS25 \uD55C\uAC15\uACF5\uC6D0\uC810", "Ramyeon by the Han River.", 20, "https://images.unsplash.com/photo-1628532431030-3b6d433ed166?q=80&w=600&auto=format&fit=crop", 37.5169, 126.9358),
        ),
        imageUrl = "https://images.unsplash.com/photo-1760020890915-ca605575b93b?q=80&w=600&auto=format&fit=crop",
    ),
    Course(
        id = "goblin-filming",
        title = "Goblin Filming Course",
        subtitle = "Walk through iconic scenes of the legendary K-Drama",
        category = "kdrama",
        region = "seoul",
        emoji = "\uD83C\uDFAC",
        duration = "3-4h",
        spotCount = 5,
        rating = 4.7f,
        tags = listOf("K-Drama", "Goblin", "Filming"),
        spots = listOf(
            Spot("gb1", "Deoksugung Stone Wall Road", "\uB355\uC218\uAD81 \uB3CC\uB2F4\uAE38", "The romantic stone wall road.", 0, "https://images.unsplash.com/photo-1748835600895-8ff48c51c37f?q=80&w=600&auto=format&fit=crop", 37.5659, 126.9751),
            Spot("gb2", "Bukchon Hanok Village", "\uBD81\uCD0C\uD55C\uC625\uB9C8\uC744", "Traditional village from the drama.", 20, "https://images.unsplash.com/photo-1704240699154-da9e9c690373?q=80&w=600&auto=format&fit=crop", 37.5826, 126.9857),
            Spot("gb3", "Incheon Open Port Area", "\uC778\uCC9C \uAC1C\uD56D\uC7A5", "The Grim Reaper's tea shop area.", 30, "https://images.unsplash.com/photo-1768711699153-bd696267e52f?q=80&w=600&auto=format&fit=crop", 37.4739, 126.6231),
        ),
        imageUrl = "https://images.unsplash.com/photo-1748835600895-8ff48c51c37f?q=80&w=600&auto=format&fit=crop",
    ),
    Course(
        id = "bts-busan",
        title = "BTS Busan Course",
        subtitle = "Visit the places where BTS members grew up",
        category = "kpop",
        region = "busan",
        emoji = "\uD83D\uDC9C",
        duration = "4-5h",
        spotCount = 4,
        rating = 4.9f,
        tags = listOf("BTS", "Busan", "ARMY"),
        spots = listOf(
            Spot("bts1", "Jimin's Dance School", "\uC9C0\uBBFC \uB304\uC2A4 \uC2A4\uCFE8", "Where BTS Jimin trained.", 0, "https://images.unsplash.com/photo-1762440775708-7dbfe9e10842?q=80&w=600&auto=format&fit=crop", 35.1525, 129.0596),
            Spot("bts2", "Gamcheon Culture Village", "\uAC10\uCC9C\uBB38\uD654\uB9C8\uC744", "Colorful hillside village.", 25, "https://images.unsplash.com/photo-1762440775708-7dbfe9e10842?q=80&w=600&auto=format&fit=crop", 35.0978, 129.0107),
            Spot("bts3", "Haeundae Beach", "\uD574\uC6B4\uB300 \uD574\uBCC0", "Featured in BTS MVs.", 30, "https://images.unsplash.com/photo-1768081977305-b5db21d91ec1?q=80&w=600&auto=format&fit=crop", 35.1586, 129.1603),
        ),
        imageUrl = "https://images.unsplash.com/photo-1762440775708-7dbfe9e10842?q=80&w=600&auto=format&fit=crop",
    ),
    Course(
        id = "local-market",
        title = "Seoul Local Market Hopping",
        subtitle = "Experience the real Korea at traditional markets",
        category = "local",
        region = "seoul",
        emoji = "\uD83C\uDFEA",
        duration = "3-4h",
        spotCount = 4,
        rating = 4.7f,
        tags = listOf("Local", "Market", "Street Food"),
        spots = listOf(
            Spot("lm1", "Gwangjang Market", "\uAD11\uC7A5\uC2DC\uC7A5", "Seoul's oldest market.", 0, "https://images.unsplash.com/photo-1628532431030-3b6d433ed166?q=80&w=600&auto=format&fit=crop", 37.5700, 126.9990),
            Spot("lm2", "Tongin Market", "\uD1B5\uC778\uC2DC\uC7A5", "Build your own dosirak.", 20, "https://images.unsplash.com/photo-1628532429788-c35922b5e6c1?q=80&w=600&auto=format&fit=crop", 37.5752, 126.9709),
            Spot("lm3", "Mangwon Market", "\uB9DD\uC6D0\uC2DC\uC7A5", "The hipsters' market.", 25, "https://images.unsplash.com/photo-1540138279543-b3728f037467?q=80&w=600&auto=format&fit=crop", 37.5560, 126.9072),
        ),
        imageUrl = "https://images.unsplash.com/photo-1628532431030-3b6d433ed166?q=80&w=600&auto=format&fit=crop",
    ),
)

// ── Fallback Routes (더미 경로 데이터) ──

data class RoutePoint(val lat: Double, val lng: Double)

data class FallbackRoute(
    val placeId: String,
    val mode: String,
    val points: List<RoutePoint>,
    val distanceMeters: Int,
    val durationMin: Int,
)

// 송정동 SSAFY 부근 (35.0950, 128.8560) 에서 각 장소까지 경로
val FALLBACK_ROUTES = mapOf(
    // dm3 신호동 해안산책로
    ("dm3" to "walk") to FallbackRoute("dm3", "walk", listOf(
        RoutePoint(35.0950, 128.8560), RoutePoint(35.0900, 128.8650),
        RoutePoint(35.0850, 128.8720), RoutePoint(35.0807, 128.8785),
    ), 2500, 30),
    ("dm3" to "car") to FallbackRoute("dm3", "car", listOf(
        RoutePoint(35.0950, 128.8560), RoutePoint(35.0870, 128.8680),
        RoutePoint(35.0807, 128.8785),
    ), 3500, 8),
    ("dm3" to "transit") to FallbackRoute("dm3", "transit", listOf(
        RoutePoint(35.0950, 128.8560), RoutePoint(35.0880, 128.8700),
        RoutePoint(35.0807, 128.8785),
    ), 4000, 15),

    // dm4 낙동강하구에코센터
    ("dm4" to "walk") to FallbackRoute("dm4", "walk", listOf(
        RoutePoint(35.0950, 128.8560), RoutePoint(35.0980, 128.8900),
        RoutePoint(35.1010, 128.9200), RoutePoint(35.1044, 128.9459),
    ), 8500, 105),
    ("dm4" to "car") to FallbackRoute("dm4", "car", listOf(
        RoutePoint(35.0950, 128.8560), RoutePoint(35.1000, 128.9100),
        RoutePoint(35.1044, 128.9459),
    ), 12000, 20),
    ("dm4" to "transit") to FallbackRoute("dm4", "transit", listOf(
        RoutePoint(35.0950, 128.8560), RoutePoint(35.0990, 128.9000),
        RoutePoint(35.1044, 128.9459),
    ), 13000, 35),

    // dm5 김해국제공항
    ("dm5" to "walk") to FallbackRoute("dm5", "walk", listOf(
        RoutePoint(35.0950, 128.8560), RoutePoint(35.1200, 128.8800),
        RoutePoint(35.1500, 128.9100), RoutePoint(35.1795, 128.9383),
    ), 10000, 130),
    ("dm5" to "car") to FallbackRoute("dm5", "car", listOf(
        RoutePoint(35.0950, 128.8560), RoutePoint(35.1400, 128.9000),
        RoutePoint(35.1795, 128.9383),
    ), 14000, 20),
    ("dm5" to "transit") to FallbackRoute("dm5", "transit", listOf(
        RoutePoint(35.0950, 128.8560), RoutePoint(35.1300, 128.9000),
        RoutePoint(35.1795, 128.9383),
    ), 15000, 40),

    // dm6 대저생태공원
    ("dm6" to "walk") to FallbackRoute("dm6", "walk", listOf(
        RoutePoint(35.0950, 128.8560), RoutePoint(35.1300, 128.9000),
        RoutePoint(35.1700, 128.9400), RoutePoint(35.2110, 128.9722),
    ), 9500, 150),
    ("dm6" to "car") to FallbackRoute("dm6", "car", listOf(
        RoutePoint(35.0950, 128.8560), RoutePoint(35.1500, 128.9200),
        RoutePoint(35.2110, 128.9722),
    ), 15000, 25),
    ("dm6" to "transit") to FallbackRoute("dm6", "transit", listOf(
        RoutePoint(35.0950, 128.8560), RoutePoint(35.1400, 128.9100),
        RoutePoint(35.2110, 128.9722),
    ), 16000, 45),

    // dm7 장림포구
    ("dm7" to "walk") to FallbackRoute("dm7", "walk", listOf(
        RoutePoint(35.0950, 128.8560), RoutePoint(35.0850, 128.9000),
        RoutePoint(35.0780, 128.9350), RoutePoint(35.0720, 128.9650),
    ), 7000, 90),
    ("dm7" to "car") to FallbackRoute("dm7", "car", listOf(
        RoutePoint(35.0950, 128.8560), RoutePoint(35.0800, 128.9200),
        RoutePoint(35.0720, 128.9650),
    ), 11000, 18),
    ("dm7" to "transit") to FallbackRoute("dm7", "transit", listOf(
        RoutePoint(35.0950, 128.8560), RoutePoint(35.0830, 128.9100),
        RoutePoint(35.0720, 128.9650),
    ), 12000, 30),
)

val HOME_RECOMMENDATIONS = listOf(
    RecommendationSection(
        title = "Based on Your Picks",
        icon = Icons.Filled.BarChart,
        label = "Big Data",
        places = listOf(
            Place("p3", "Jinudo Island", "\uC9C4\uC6B0\uB3C4", 4.7f, "6.0km", "Nature", CatAttraction, lat = 35.0850, lng = 128.9200, imageUrl = "https://images.unsplash.com/photo-1682090369590-c4c82f3cc065?q=80&w=600&auto=format&fit=crop"),
            Place("p4", "Amisan Observatory", "\uC544\uBBF8\uC0B0\uC804\uB9DD\uB300", 4.8f, "8.0km", "Nature", CatAttraction, lat = 35.0530, lng = 128.9580, imageUrl = "https://images.unsplash.com/photo-1768006273763-85c9ff25e5fa?q=80&w=600&auto=format&fit=crop"),
            Place("p5", "Dadaepo Beach", "\uB2E4\uB300\uD3EC\uD574\uC218\uC695\uC7A5", 4.6f, "9.5km", "Nature", CatAttraction, lat = 35.0470, lng = 128.9660, imageUrl = "https://images.unsplash.com/photo-1617577367443-2d778fedeef4?q=80&w=600&auto=format&fit=crop"),
        ),
        btnText = "Show more nature spots",
    ),
    RecommendationSection(
        title = "Personalized for You",
        icon = Icons.Filled.AutoAwesome,
        label = "Male \u00B7 20s",
        places = listOf(
            Place("dm3", "Sinho Beach Trail", "\uC2E0\uD638\uB3D9 \uD574\uC548\uC0B0\uCC45\uB85C", 4.7f, "2.5km", "Nature", CatAttraction, lat = 35.0807, lng = 128.8785, imageUrl = "https://images.unsplash.com/photo-1517154421773-0529f29ea451?q=80&w=600&auto=format&fit=crop"),
            Place("dm4", "Nakdong Estuary Eco Center", "\uB099\uB3D9\uAC15\uD558\uAD6C\uC5D0\uCF54\uC13C\uD130", 4.8f, "8.5km", "Nature", CatAttraction, lat = 35.1044, lng = 128.9459, imageUrl = "https://images.unsplash.com/photo-1588668214407-6ea9a6d8c272?q=80&w=600&auto=format&fit=crop"),
            Place("dm7", "Jangnim Port", "\uC7A5\uB9BC\uD3EC\uAD6C", 4.5f, "7.0km", "Food", CatFood, lat = 35.0720, lng = 128.9650, imageUrl = "https://images.unsplash.com/photo-1540138279543-b3728f037467?q=80&w=600&auto=format&fit=crop"),
        ),
        btnText = "Show more near Songjeong",
    ),
)