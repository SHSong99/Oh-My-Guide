package com.ohmyguide.app.domain.model

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NaviRouteCache @Inject constructor() {
    private var data: NaviRouteData? = null

    fun store(route: NaviRouteData) {
        data = route
    }

    fun peek(): NaviRouteData? = data
}
