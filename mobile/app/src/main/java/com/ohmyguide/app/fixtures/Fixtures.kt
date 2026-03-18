package com.ohmyguide.app.fixtures

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
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
)

data class PlaceDetail(
    val place: Place,
    val desc: String,
    val hours: String,
    val fee: String,
    val walkTime: String,
)

val SAMPLE_PLACES = listOf(
    Place("dm3", "Gwangjang Market", "\uAD11\uC7A5\uC2DC\uC7A5", 4.8f, "350m", "Food", CatFood, "\uD83E\uDD58"),
    Place("dm4", "Bukchon Hanok Village", "\uBD81\uCD0C\uD55C\uC625\uB9C8\uC744", 4.6f, "1.2km", "Culture", CatCulture, "\uD83C\uDFD8\uFE0F"),
    Place("dm5", "Namsan Tower", "\uB0A8\uC0B0\uD0C0\uC6CC", 4.7f, "2.1km", "Nature", CatAttraction, "\uD83D\uDDFC"),
    Place("dm6", "Ikseon-dong", "\uC775\uC120\uB3D9", 4.5f, "600m", "Culture", CatCulture, "\uD83C\uDFD8\uFE0F"),
    Place("dm7", "Cheonggyecheon Stream", "\uCCAD\uACC4\uCC9C", 4.7f, "400m", "Nature", CatAttraction, "\uD83C\uDF0A"),
)

val SAMPLE_PLACE_DETAILS = mapOf(
    "dm3" to PlaceDetail(
        place = SAMPLE_PLACES[0],
        desc = "One of the oldest and largest traditional markets in South Korea. Famous for its bindaetteok (mung bean pancakes) and other street food delicacies.",
        hours = "09:00 - 18:00",
        fee = "Free",
        walkTime = "5 min walk",
    ),
    "dm4" to PlaceDetail(
        place = SAMPLE_PLACES[1],
        desc = "A traditional Korean village with hundreds of hanok houses dating back to the Joseon dynasty. Beautiful blend of old and new Seoul.",
        hours = "10:00 - 17:00",
        fee = "Free",
        walkTime = "15 min walk",
    ),
    "dm5" to PlaceDetail(
        place = SAMPLE_PLACES[2],
        desc = "Iconic tower offering panoramic views of Seoul. A must-visit landmark and popular romantic destination.",
        hours = "10:00 - 23:00",
        fee = "\u20A916,000",
        walkTime = "25 min walk",
    ),
    "dm6" to PlaceDetail(
        place = SAMPLE_PLACES[3],
        desc = "A charming alley of renovated 1920s hanok houses, now filled with trendy cafes, restaurants, and boutique shops.",
        hours = "10:00 - 22:00",
        fee = "Free",
        walkTime = "8 min walk",
    ),
    "dm7" to PlaceDetail(
        place = SAMPLE_PLACES[4],
        desc = "A restored urban stream stretching 10.9km through downtown Seoul. A peaceful walking path with public art and fountains.",
        hours = "Open 24h",
        fee = "Free",
        walkTime = "5 min walk",
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

data class LanguageOption(val id: String, val label: String)
data class CompanionOption(val id: String, val label: String, val emoji: String)
data class CountryOption(val id: String, val flag: String, val name: String)

val LANGUAGE_OPTIONS = listOf(
    LanguageOption("en", "English"),
    LanguageOption("ja", "\u65E5\u672C\u8A9E"),
    LanguageOption("zh", "\u4E2D\u6587"),
)

val GENDER_OPTIONS = listOf("Female", "Male")

val COMPANION_OPTIONS = listOf(
    CompanionOption("friends", "Friends", "\uD83D\uDC6B"),
    CompanionOption("family", "Family", "\uD83D\uDC68\u200D\uD83D\uDC69\u200D\uD83D\uDC67"),
    CompanionOption("solo", "Solo", "\uD83C\uDF92"),
    CompanionOption("partner", "Partner", "\u2764\uFE0F"),
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
)

data class Region(val id: String, val name: String)

data class ExploreCategoryGroup(
    val key: String,
    val label: String,
    val sub: String,
    val emoji: String,
    val color: Color,
    val bgColor: Color,
)

val EXPLORE_REGIONS = listOf(
    Region("all", "All"),
    Region("seoul", "Seoul"),
    Region("busan", "Busan"),
    Region("jeju", "Jeju"),
    Region("gyeongju", "Gyeongju"),
    Region("incheon", "Incheon"),
    Region("jeonju", "Jeonju"),
)

val EXPLORE_CATEGORY_GROUPS = listOf(
    ExploreCategoryGroup("local", "Local Life", "Markets & food", "\uD83C\uDFEA", Success, Color(0xFFF0FDF4)),
    ExploreCategoryGroup("kpop", "K-Pop", "Idol hotspots", "\uD83C\uDFB5", Color(0xFF7C3AED), Color(0xFFF5F3FF)),
    ExploreCategoryGroup("kdrama", "K-Drama", "Filming spots", "\uD83C\uDFAC", Color(0xFFEA580C), Color(0xFFFFF7ED)),
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
            Spot("dh1", "HYBE Insight", "\uD558\uC774\uBE0C \uC778\uC0AC\uC774\uD2B8", "The official museum of HYBE entertainment.", 0),
            Spot("dh2", "Hongdae Busking Stage", "\uD64D\uB300 \uBC84\uC2A4\uD0B9 \uBB34\uB300", "The legendary busking area.", 15),
            Spot("dh3", "SM Entertainment Caf\u00E9", "SM \uC5D4\uD130 \uCE74\uD398", "Official SM caf\u00E9 with themed drinks.", 10),
        ),
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
            Spot("cvs1", "CU Flagship Seongsu", "CU \uC131\uC218 \uD50C\uB798\uADF8\uC2ED", "Korea's trendiest CU store.", 0),
            Spot("cvs2", "GS25 Hangang Park", "GS25 \uD55C\uAC15\uACF5\uC6D0\uC810", "Ramyeon by the Han River.", 20),
        ),
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
            Spot("gb1", "Deoksugung Stone Wall Road", "\uB355\uC218\uAD81 \uB3CC\uB2F4\uAE38", "The romantic stone wall road.", 0),
            Spot("gb2", "Bukchon Hanok Village", "\uBD81\uCD0C\uD55C\uC625\uB9C8\uC744", "Traditional village from the drama.", 20),
            Spot("gb3", "Incheon Open Port Area", "\uC778\uCC9C \uAC1C\uD56D\uC7A5", "The Grim Reaper's tea shop area.", 30),
        ),
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
            Spot("bts1", "Jimin's Dance School", "\uC9C0\uBBFC \uB304\uC2A4 \uC2A4\uCFE8", "Where BTS Jimin trained.", 0),
            Spot("bts2", "Gamcheon Culture Village", "\uAC10\uCC9C\uBB38\uD654\uB9C8\uC744", "Colorful hillside village.", 25),
            Spot("bts3", "Haeundae Beach", "\uD574\uC6B4\uB300 \uD574\uBCC0", "Featured in BTS MVs.", 30),
        ),
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
            Spot("lm1", "Gwangjang Market", "\uAD11\uC7A5\uC2DC\uC7A5", "Seoul's oldest market.", 0),
            Spot("lm2", "Tongin Market", "\uD1B5\uC778\uC2DC\uC7A5", "Build your own dosirak.", 20),
            Spot("lm3", "Mangwon Market", "\uB9DD\uC6D0\uC2DC\uC7A5", "The hipsters' market.", 25),
        ),
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

// 시청 부근 (37.5665, 126.9780) 에서 각 장소까지 경로
val FALLBACK_ROUTES = mapOf(
    // dm3 광장시장
    ("dm3" to "walk") to FallbackRoute("dm3", "walk", listOf(
        RoutePoint(37.5665, 126.9780), RoutePoint(37.5672, 126.9830),
        RoutePoint(37.5680, 126.9880), RoutePoint(37.5690, 126.9940),
        RoutePoint(37.5700, 126.9990),
    ), 1500, 5),
    ("dm3" to "drive") to FallbackRoute("dm3", "drive", listOf(
        RoutePoint(37.5665, 126.9780), RoutePoint(37.5670, 126.9860),
        RoutePoint(37.5695, 126.9950), RoutePoint(37.5700, 126.9990),
    ), 2100, 3),
    ("dm3" to "transit") to FallbackRoute("dm3", "transit", listOf(
        RoutePoint(37.5665, 126.9780), RoutePoint(37.5660, 126.9850),
        RoutePoint(37.5670, 126.9920), RoutePoint(37.5700, 126.9990),
    ), 2400, 12),

    // dm4 북촌한옥마을
    ("dm4" to "walk") to FallbackRoute("dm4", "walk", listOf(
        RoutePoint(37.5665, 126.9780), RoutePoint(37.5700, 126.9790),
        RoutePoint(37.5740, 126.9800), RoutePoint(37.5780, 126.9815),
        RoutePoint(37.5826, 126.9831),
    ), 1800, 15),
    ("dm4" to "drive") to FallbackRoute("dm4", "drive", listOf(
        RoutePoint(37.5665, 126.9780), RoutePoint(37.5720, 126.9800),
        RoutePoint(37.5790, 126.9820), RoutePoint(37.5826, 126.9831),
    ), 2500, 5),
    ("dm4" to "transit") to FallbackRoute("dm4", "transit", listOf(
        RoutePoint(37.5665, 126.9780), RoutePoint(37.5700, 126.9810),
        RoutePoint(37.5760, 126.9820), RoutePoint(37.5826, 126.9831),
    ), 3200, 10),

    // dm5 남산타워
    ("dm5" to "walk") to FallbackRoute("dm5", "walk", listOf(
        RoutePoint(37.5665, 126.9780), RoutePoint(37.5640, 126.9800),
        RoutePoint(37.5600, 126.9830), RoutePoint(37.5560, 126.9860),
        RoutePoint(37.5512, 126.9882),
    ), 2100, 25),
    ("dm5" to "drive") to FallbackRoute("dm5", "drive", listOf(
        RoutePoint(37.5665, 126.9780), RoutePoint(37.5620, 126.9820),
        RoutePoint(37.5550, 126.9860), RoutePoint(37.5512, 126.9882),
    ), 3500, 8),
    ("dm5" to "transit") to FallbackRoute("dm5", "transit", listOf(
        RoutePoint(37.5665, 126.9780), RoutePoint(37.5630, 126.9810),
        RoutePoint(37.5570, 126.9850), RoutePoint(37.5512, 126.9882),
    ), 4000, 15),

    // dm6 익선동
    ("dm6" to "walk") to FallbackRoute("dm6", "walk", listOf(
        RoutePoint(37.5665, 126.9780), RoutePoint(37.5690, 126.9830),
        RoutePoint(37.5710, 126.9870), RoutePoint(37.5735, 126.9920),
    ), 600, 8),
    ("dm6" to "drive") to FallbackRoute("dm6", "drive", listOf(
        RoutePoint(37.5665, 126.9780), RoutePoint(37.5700, 126.9850),
        RoutePoint(37.5735, 126.9920),
    ), 900, 3),
    ("dm6" to "transit") to FallbackRoute("dm6", "transit", listOf(
        RoutePoint(37.5665, 126.9780), RoutePoint(37.5680, 126.9850),
        RoutePoint(37.5710, 126.9890), RoutePoint(37.5735, 126.9920),
    ), 1200, 8),

    // dm7 청계천
    ("dm7" to "walk") to FallbackRoute("dm7", "walk", listOf(
        RoutePoint(37.5665, 126.9780), RoutePoint(37.5675, 126.9780),
        RoutePoint(37.5690, 126.9780),
    ), 400, 5),
    ("dm7" to "drive") to FallbackRoute("dm7", "drive", listOf(
        RoutePoint(37.5665, 126.9780), RoutePoint(37.5690, 126.9780),
    ), 500, 2),
    ("dm7" to "transit") to FallbackRoute("dm7", "transit", listOf(
        RoutePoint(37.5665, 126.9780), RoutePoint(37.5670, 126.9790),
        RoutePoint(37.5690, 126.9780),
    ), 800, 5),
)

val HOME_RECOMMENDATIONS = listOf(
    RecommendationSection(
        title = "Based on Your Picks",
        icon = Icons.Filled.BarChart,
        label = "Big Data",
        places = listOf(
            Place("p3", "Hyundai Card Music Library", "\uD604\uB300\uCE74\uB4DC \uBBA4\uC9C1 \uB77C\uC774\uBE0C\uB7EC\uB9AC", 4.7f, "2.5km", "Culture", CatCulture),
            Place("p4", "Seoul Forest", "\uC11C\uC6B8\uC232", 4.8f, "4.1km", "Nature", CatAttraction),
            Place("p5", "Onion Anguk", "\uC5B4\uB2C8\uC5B8 \uC548\uAD6D", 4.6f, "800m", "Caf\u00E9", CatCafe),
        ),
        btnText = "Show more romantic spots",
    ),
    RecommendationSection(
        title = "Personalized for You",
        icon = Icons.Filled.AutoAwesome,
        label = "Male \u00B7 20s",
        places = listOf(
            Place("dm3", "Gwangjang Market", "\uAD11\uC7A5\uC2DC\uC7A5", 4.8f, "1.5km", "Food", CatFood),
            Place("cm_m1", "Kasina Hannam", "\uCE74\uC2DC\uB098 \uD55C\uB0A8", 4.7f, "2.8km", "Streetwear", CatCulture),
            Place("cm_m2", "Anthracite Coffee", "\uC564\uD2B8\uB7EC\uC0AC\uC774\uD2B8", 4.6f, "1.2km", "Caf\u00E9", CatCafe),
        ),
        btnText = "Show more for Men in 20s",
    ),
)