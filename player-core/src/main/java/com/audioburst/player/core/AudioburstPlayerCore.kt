package com.audioburst.player.core

import android.content.Context
import com.audioburst.library.AudioburstLibrary
import com.audioburst.library.models.*
import com.audioburst.player.core.AudioburstPlayerCore.init
import com.audioburst.player.core.di.Injector
import com.audioburst.player.core.media.BurstPlayer
import com.audioburst.player.core.media.MediaSessionConnection
import kotlinx.coroutines.flow.Flow

/**
 * Main library's entry point. This class lets you access Audioburst data and control playback.
 * Playback functions of this class delegates responsibility to [BurstPlayer], so if you are searching for
 * better control of the playback you should obtain the instance of this interface.
 *
 * [init] function should be called from within your [Application] class with application [Context] as
 * a parameter.
 */
public object AudioburstPlayerCore {

    internal lateinit var mediaSessionConnection: MediaSessionConnection
    private const val ERROR_MESSAGE = "Library is not initialized. You should call AudioburstPlayerCore.init first."

    internal var _burstPlayer: BurstPlayer? = null
    /**
     * Obtain this object to better control playback, observe its state and much more.
     */
    public val burstPlayer: BurstPlayer
        get() = if (_burstPlayer == null) {
            error(ERROR_MESSAGE)
        } else {
            _burstPlayer!!
        }

    internal var _audioburstLibrary: AudioburstLibrary? = null
    /**
     * Use [AudioburstLibrary] to request Audioburst content.
     */
    public val audioburstLibrary: AudioburstLibrary
        get() = if (_audioburstLibrary == null) {
            error(ERROR_MESSAGE)
        } else {
            _audioburstLibrary!!
        }

    /**
     * The function that initializes library. It should be used in the entry point of your application
     * (class that extends [Application]).
     *
     * @param context Context of the application.
     * @param applicationKey Key obtained from Audioburst Publishers (https://publishers.audioburst.com/).
     */
    public fun init(context: Context, applicationKey: String) {
        Injector.init(context, applicationKey)
        Injector.inject(this)
        initMediaSession()
    }

    private fun initMediaSession() {
        mediaSessionConnection.connect()
    }

    /**
     * Use this function to get all of the available [PlaylistInfo].
     *
     * @return [Result.Data] when it was possible to get requested resource. In case there was a problem getting it
     * [Result.Error] will be returned with a proper ([LibraryError]).
     */
    public suspend fun getPlaylists(): Result<List<PlaylistInfo>> =
        audioburstLibrary.getPlaylists()

    /**
     * Personal playlist is a special type of playlist that is built with user preferences in mind. Sometimes it takes
     * more time to prepare a personal playlist that is why library exposes an ability to "subscribe" to ongoing changes
     * to personal playlist. By subscribing you will be notified every time there are new [Burst]`s in the playlist until
     * playlist is ready. You can check whether playlist is ready by querying [PendingPlaylist.isReady] value.
     *
     * @return [Flow] of [Result] of [PendingPlaylist] which describes whether whole playlist is ready.
     */
    public suspend fun getPersonalPlaylist(): Flow<Result<PendingPlaylist>> =
        audioburstLibrary.getPersonalPlaylist()

    /**
     * Use this function to get information about chosen [Playlist].
     *
     * @return [Result.Data] when it was possible to get requested resource. In case there was a problem getting it
     * [Result.Error] will be returned with a proper ([LibraryError]).
     */
    public suspend fun getPlaylist(playlistInfo: PlaylistInfo): Result<Playlist> =
        audioburstLibrary.getPlaylist(playlistInfo)

    /**
     * You can use this function to pass previously recorded and converted to [ByteArray] search query.
     *
     * @return [Result.Data] when it was possible to get requested resource. In case there was a problem getting it
     * [Result.Error] will be returned with a proper ([LibraryError]).
     */
    public suspend fun getPlaylist(byteArray: ByteArray): Result<Playlist> =
        audioburstLibrary.getPlaylist(byteArray)

    /**
     * If you already have users in your app and you wouldn't like to register new one, you can use this function to
     * inform library what ABUserId it should use to communicate to the API. This function will return true if the given
     * ABUserId is correct and present in Audioburst database. Otherwise it will return false.
     *
     * @return [Result.Data] when it was possible to get requested resource. In case there was a problem getting it
     * [Result.Error] will be returned with a proper ([LibraryError]).
     */
    public suspend fun setAudioburstUserID(userId: String): Result<Boolean> =
        audioburstLibrary.setAudioburstUserID(userId)

    /**
     * [Burst] class exposes nullable [CtaData], which you can use to show a CTA (Call to action) button which prompts
     * the user to an immediate response. The CtaData, when available, provides the text to be shown on the button
     * (buttonText) and the link (url) to open in the browser upon clicking the button.
     *
     * When the user clicks this button, you should call the following function to inform the library about this.
     *
     * @param burstId Id of the [Burst] whose CTA button has been clicked.
     */
    public fun ctaButtonClick(burstId: String) {
        audioburstLibrary.ctaButtonClick(burstId)
    }

    /**
     * By default Library will filter-out all already listened by the user [Burst]s. Use this function to change this behaviour.
     *
     * @param enabled Controls whether already listened [Burst]s should be filtered-out
     */
    public fun filterListenedBursts(enabled: Boolean) {
        audioburstLibrary.filterListenedBursts(enabled)
    }

    /**
     * Loads a [Playlist].
     *
     * @param playlist The [Playlist] to load.
     * @param playWhenReady Whether playback should proceed when ready.
     */
    public fun load(playlist: Playlist, playWhenReady: Boolean) {
        burstPlayer.load(playlist, playWhenReady)
    }

    /**
     * Trying to start playback if there is any [Playlist] ready.
     */
    public fun play() {
        burstPlayer.play()
    }

    /**
     * Pauses playback.
     */
    public fun pause() {
        burstPlayer.pause()
    }

    /**
     * Trying to move to the next [Burst].
     *
     * @return Whether it was possible to move to next.
     */
    public fun next(): Boolean =
        burstPlayer.next()

    /**
     * Trying to move to the previous [Burst].
     *
     * @return Whether it was possible to move to previous.
     */
    public fun previous(): Boolean =
        burstPlayer.previous()
}