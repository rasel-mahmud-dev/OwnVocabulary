package com.rs.myvocabulary.utils

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object MediaPlaybackManager {
    private var exoPlayer: ExoPlayer? = null
    private val _currentUrl = MutableStateFlow<String?>(null)
    val currentUrl: StateFlow<String?> = _currentUrl

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    @OptIn(androidx.media3.common.util.UnstableApi::class)
    fun getPlayer(context: Context): ExoPlayer {
        if (exoPlayer == null) {
            val loadControl =
                    androidx.media3.exoplayer.DefaultLoadControl.Builder()
                            .setBufferDurationsMs(15000, 50000, 1500, 2000)
                            .build()

            val dataSourceFactory =
                    androidx.media3.datasource.DefaultHttpDataSource.Factory()
                            .setAllowCrossProtocolRedirects(true)
                            .setConnectTimeoutMs(10000)
                            .setReadTimeoutMs(10000)

            val mediaSourceFactory =
                    androidx.media3.exoplayer.source.DefaultMediaSourceFactory(context)
                            .setDataSourceFactory(dataSourceFactory)

            exoPlayer =
                    ExoPlayer.Builder(context.applicationContext)
                            .setLoadControl(loadControl)
                            .setMediaSourceFactory(mediaSourceFactory)
                            .build()

            exoPlayer?.addListener(
                    object : Player.Listener {
                        override fun onIsPlayingChanged(playing: Boolean) {
                            _isPlaying.value = playing
                        }
                        override fun onPlaybackStateChanged(playbackState: Int) {
                            if (playbackState == Player.STATE_ENDED) {
                                _currentUrl.value = null
                                _isPlaying.value = false
                            }
                        }
                    }
            )
        }
        return exoPlayer!!
    }

    fun play(context: Context, source: Any, isVideo: Boolean = true) {
        val player = getPlayer(context)
        val sourceStr = source.toString()

        if (_currentUrl.value == sourceStr) {
            if (player.isPlaying) {
                player.pause()
            } else {
                player.play()
            }
            return
        }

        _currentUrl.value = sourceStr
        player.stop()
        player.clearMediaItems()

        player.repeatMode = if (isVideo) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF

        val mediaItem =
                when (source) {
                    is Uri -> MediaItem.fromUri(source)
                    is String -> MediaItem.fromUri(source)
                    else -> throw IllegalArgumentException("Source must be String or Uri")
                }

        player.setMediaItem(mediaItem)

        if (isVideo && source is String) {
            val lastPosition = PlaybackPreferenceManager.getPosition(context, source)
            if (lastPosition > 0) {
                player.seekTo(lastPosition)
            }
        }

        player.prepare()
        player.play()
    }

    fun pause() {
        exoPlayer?.pause()
    }

    fun stop() {
        exoPlayer?.stop()
        _currentUrl.value = null
        _isPlaying.value = false
    }

    fun release() {
        exoPlayer?.release()
        exoPlayer = null
        _currentUrl.value = null
        _isPlaying.value = false
    }
}
