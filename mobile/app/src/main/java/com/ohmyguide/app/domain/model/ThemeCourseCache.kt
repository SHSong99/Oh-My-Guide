package com.ohmyguide.app.domain.model

import com.ohmyguide.app.fixtures.Course

object ThemeCourseCache {
    private val cache = mutableMapOf<String, Course>()

    fun put(themeId: String, course: Course) {
        cache[themeId] = course
    }

    fun get(themeId: String): Course? = cache[themeId]
}
