package com.ohmyguide.app.domain.model

import com.ohmyguide.app.fixtures.KoreanPhrase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class BookmarkedPhrase(
    val key: String,
    val phrase: KoreanPhrase,
    val sectionTitle: String,
)

object PhraseBookmarkStore {
    private val _bookmarks = MutableStateFlow<Map<String, BookmarkedPhrase>>(emptyMap())
    val bookmarks: StateFlow<Map<String, BookmarkedPhrase>> = _bookmarks.asStateFlow()

    fun toggle(key: String, phrase: KoreanPhrase, sectionTitle: String) {
        val current = _bookmarks.value.toMutableMap()
        if (current.containsKey(key)) {
            current.remove(key)
        } else {
            current[key] = BookmarkedPhrase(key, phrase, sectionTitle)
        }
        _bookmarks.value = current
    }

    fun remove(key: String) {
        val current = _bookmarks.value.toMutableMap()
        current.remove(key)
        _bookmarks.value = current
    }

    fun isBookmarked(key: String): Boolean = _bookmarks.value.containsKey(key)
}
