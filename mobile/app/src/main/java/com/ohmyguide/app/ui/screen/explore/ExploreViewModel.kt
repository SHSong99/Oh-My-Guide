package com.ohmyguide.app.ui.screen.explore

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.ohmyguide.app.fixtures.FEATURED_THEMES
import com.ohmyguide.app.fixtures.FeaturedTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ExploreViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
) : ViewModel() {

    val themes: List<FeaturedTheme> = FEATURED_THEMES

    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val players = mutableMapOf<Int, ExoPlayer>()

    fun getOrCreatePlayer(index: Int): ExoPlayer {
        return players.getOrPut(index) {
            ExoPlayer.Builder(context).build().apply {
                val uri = Uri.parse(
                    "android.resource://${context.packageName}/${themes[index].videoRes}"
                )
                setMediaItem(MediaItem.fromUri(uri))
                repeatMode = Player.REPEAT_MODE_ONE
                volume = 0f
                prepare()
                playWhenReady = index == _currentPage.value
            }
        }
    }

    fun onPageChanged(page: Int) {
        _currentPage.value = page
        players.forEach { (index, player) ->
            player.playWhenReady = index == page
        }
    }

    fun pauseAll() {
        players.values.forEach { it.playWhenReady = false }
    }

    override fun onCleared() {
        super.onCleared()
        players.values.forEach { it.release() }
        players.clear()
    }
}