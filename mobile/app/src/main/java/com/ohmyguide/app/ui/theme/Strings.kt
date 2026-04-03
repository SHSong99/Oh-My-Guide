package com.ohmyguide.app.ui.theme

interface AppStrings {
    // ── Common ──
    val appName: String
    val back: String
    val send: String
    val remove: String
    val stop: String
    val skip: String
    val restore: String
    val map: String
    val next: String

    // ── Bottom Nav ──
    val navHome: String
    val navExplore: String
    val navPhrases: String

    // ── Splash ──
    val splashSubtitle: String

    // ── Welcome ──
    val welcomeGreeting: String
    val welcomeIntro: String
    val welcomeDesc: String
    val signInGoogle: String
    val signInHint: String
    val loginSuccess: String

    // ── Onboarding ──
    val onboardLangPrompt: String
    val onboardGenderPrompt: String
    val onboardAgePrompt: String
    val onboardCountryPrompt: String
    val onboardCompanionPrompt: String
    val onboardGpsPrompt: String
    val allowLocation: String
    val enterAge: String
    val yearsOld: String
    val ageConfirm: String  // "I'm 25 years old" / "25세입니다"
    val selectCountry: String
    val female: String
    val male: String
    val friends: String
    val family: String
    val solo: String
    val partner: String

    // ── Category ──
    val detectingLocation: String
    val yourLocation: String
    val categoryGreeting: String
    val categoryPrompt: String
    val categoryDone: String
    val chooseInterests: String
    val addMoreOrSend: String
    val categoriesSelected: String

    // ── Category Names ──
    val categoryNames: Map<String, String>
    val categorySubs: Map<String, String>

    // ── Loading ──
    val findingSpots: String
    val scanningNearby: String

    // ── Home ──
    val curatingSpots: String
    val curatingDone: String
    val reset: String
    val near: String
    val spots: String
    val yourArea: String
    val backToList: String
    val findOtherPlaces: String
    val goHere: String

    // ── Explore ──
    val hotThisWeek: String
    val courses: String
    val allCourses: String
    val kCulture: String
    val explore: String
    val trending: String
    val diveIntoKCulture: String
    val curatedCourses: String
    val noCoursesFound: String
    val tryDifferentRegion: String
    val exploreThisCourse: String
    val seeAllCourses: String
    val swipeToDiscover: String
    val courseRoute: String
    val startCourseNavi: String
    val courseProgress: String
    val rightHere: String

    // ── Transport ──
    val destination: String
    val tapToSeeRoutes: String
    val viewTransitRoutes: String
    val startNavigation: String
    val walk: String
    val enjoyScenery: String
    val transit: String
    val busAndSubway: String
    val taxi: String
    val taxiCar: String
    val transfer: String
    val subway: String
    val direct: String
    val stopsUnit: String

    // ── Navi ──
    val walkingTo: String
    val transitTo: String
    val drivingTo: String
    val yesShowMe: String
    val iveArrived: String
    val listen: String
    val photo: String
    val prices: String

    // ── Story ──
    val storyLabel: String
    val loading: String
    val nowPlaying: String
    val paused: String
    val backToGuide: String
    val listenToGuide: String

    // ── Phrases ──
    val koreanPhrasesTitle: String
    val koreanPhrasesSubtitle: String
    val saved: String
    val bookmarkHint: String
    val phrasesUnit: String

    // ── MyPage ──
    val myPage: String
    val visitHistory: String
    val placesVisitedDesc: String
    val bookmarks: String
    val savedPlacesDesc: String
    val storyArchive: String
    val storiesListenedDesc: String
    val language: String
    val notifications: String
    val on: String
    val theme: String
    val light: String
    val traveler: String
    val food: String
    val culture: String
    val signOut: String
    val ageUnit: String
    val pickRecommendTitle: String
    val pickRecommendDesc: String
    val pickRecommendEmpty: String
    val editProfile: String
    val nationality: String
    val age: String
    val gender: String
    val save: String
    val profileFieldsRequired: String
    val bookmarkEmpty: String
    val viewAllPhrases: String

    // ── Chat / Recommend ──
    val picksForYou: String
    val newPicksForYou: String
    val noPlacesFound: String
    val sorryNoPlaces: String
    val freshPicks: String
    val mainFocusQuestion: String
    val vibeQuestion: String
    val optionFood: String
    val optionPhoto: String
    val optionShopping: String
    val optionActive: String
    val optionCalm: String
    val optionNightlife: String
    val showMore: String
    val moreSpots: String

    // ── Navi Chat ──
    val guideToPlace: String
    val storyAboutPlace: String
    val usefulPhrases: String
    val arrivedAt: String
    val naviCourseSpotProgress: String
    val naviTransferAhead: String
    val naviFollowTransit: String
    val naviDriveTime: String
    val naviWalkTime: String
    val naviArrivalPrompt: String
    val naviNearbySpot: String

    // ── Weather ──
    val weatherIntro: String
    val weatherClear: String
    val weatherMainlyClear: String
    val weatherPartlyCloudy: String
    val weatherOvercast: String
    val weatherFoggy: String
    val weatherDrizzle: String
    val weatherRain: String
    val weatherFreezingRain: String
    val weatherSnow: String
    val weatherSnowGrains: String
    val weatherRainShowers: String
    val weatherSnowShowers: String
    val weatherThunderstorm: String
    val weatherThunderstormHail: String
    val weatherUnknown: String
    val weatherTipHot: String
    val weatherTipWarm: String
    val weatherTipFeelsCold: String
    val weatherTipMild: String
    val weatherTipCold: String
    val weatherTipStrongWind: String
    val weatherTipWindy: String
    val weatherTipRaining: String
    val weatherTipSnowing: String
    val weatherTipThunderstorm: String
    val weatherTipRainChance: String
    val weatherTipDark: String
    val weatherTipGreat: String
    val weatherTipClearNight: String

    // ── Map ──
    val locating: String
    val mapView: String
    val explorePlaces: String

    // ── Navi Story ──
    val storyPromptHint: String

    // ── Place ──
    val hours: String
    val fee: String
    val distance: String
    val preview: String
    val placeNo: String
    val placeGo: String

    // ── Transit Format ──
    val transitRoutes: String
    val searchingRoutes: String
    val noTransitRoutes: String
    val tryWalkOrCar: String
    val filterAll: String
    val busLabel: String
    val busSubwayLabel: String
    val minSuffix: String
    val hourSuffix: String
    val minWalkSuffix: String
    val etaPrefix: String
    val notAvailable: String
    val stopsAway: String
    val listenToStory: String
    val getOffAt: String
    val currentLocation: String

    // ── Confirm Dialog ──
    val endNaviTitle: String
    val endNaviMessage: String
    val confirm: String
    val cancel: String

    // ── Course Toast ──
    val movingToNextCourse: String

    // ── Rating ──
    val ratingTitle: String
    val ratingSubtitle: String
    val ratingSubmit: String

    // ── Empty State ──
    val noRecommendations: String
    val noRecommendationsDesc: String
    val noSavedPhrases: String
    val noSavedPhrasesDesc: String
}