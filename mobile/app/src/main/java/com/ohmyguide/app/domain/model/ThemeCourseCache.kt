package com.ohmyguide.app.domain.model

import com.ohmyguide.app.fixtures.Course
import java.util.concurrent.ConcurrentHashMap

object ThemeCourseCache {
    private val cache = ConcurrentHashMap<String, Course>()

    fun put(themeId: String, course: Course) {
        cache[themeId] = course
    }

    fun get(themeId: String): Course? = cache[themeId]
}
