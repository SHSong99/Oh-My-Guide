package com.ohmyguide.app.domain.model

import com.ohmyguide.app.data.model.GuideNavigationResponse
import com.ohmyguide.app.fixtures.PlaceDetail

object PlaceDetailCache {
    private val cache = mutableMapOf<String, PlaceDetail>()
    private val guideCache = mutableMapOf<String, GuideNavigationResponse>()

    fun put(placeId: String, detail: PlaceDetail) {
        cache[placeId] = detail
    }

    fun get(placeId: String): PlaceDetail? = cache[placeId]

    fun putGuide(placeId: String, guide: GuideNavigationResponse) {
        guideCache[placeId] = guide
    }

    fun getGuide(placeId: String): GuideNavigationResponse? = guideCache[placeId]

    fun clear() {
        cache.clear()
        guideCache.clear()
    }
}
