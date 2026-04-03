package com.ohmyguide.app.domain.model

import com.ohmyguide.app.fixtures.KoreanPhrase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class BookmarkedPhrase(
    val key: String,
    val phrase: KoreanPhrase,
    val sectionTitle: String,
)

object PhraseBookmarkStore {
    private val _bookmarks = MutableStateFlow<Map<String, BookmarkedPhrase>>(emptyMap())
    val bookmarks: StateFlow<Map<String, BookmarkedPhrase>> = _bookmarks.asStateFlow()

    fun toggle(key: String, phrase: KoreanPhrase, sectionTitle: String) {
        _bookmarks.update { current ->
            if (current.containsKey(key)) {
                current - key
            } else {
                current + (key to BookmarkedPhrase(key, phrase, sectionTitle))
            }
        }
    }

    fun remove(key: String) {
        _bookmarks.update { it - key }
    }

    fun isBookmarked(key: String): Boolean = _bookmarks.value.containsKey(key)
}
