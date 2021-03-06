package com.audioburst.player.core

import android.content.Context
import android.content.pm.PackageManager
import com.audioburst.library.AudioburstLibrary
import com.audioburst.library.models.*
import com.audioburst.player.core.AudioburstPlayerCore.init
import com.audioburst.player.core.di.Injector
import com.audioburst.player.core.media.BurstPlayer
import com.audioburst.player.core.media.MediaSessionConnection
import com.audioburst.player.core.media.PlaybackNotificationController
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
    internal lateinit var playbackNotificationController: PlaybackNotificationController
    private const val APP_KEY_METADATA_KEY = "com.audioburst.applicationKey"
    private const val INIT_ERROR_MESSAGE = "Library is not initialized. You should call AudioburstPlayerCore.init first."
    private const val MISSING_METADATA_ERROR_MESSAGE = "You need to either call AudioburstPlayerCore.init(context, applicationKey) first or put applicationKey into the AndroidManifest.xml under meta-data tag with \"$APP_KEY_METADATA_KEY\" key."

    internal var _burstPlayer: BurstPlayer? = null
    /**
     * Obtain this object to better control playback, observe its state and much more.
     */
    @JvmStatic
    public val burstPlayer: BurstPlayer
        get() = if (_burstPlayer == null) {
            error(INIT_ERROR_MESSAGE)
        } else {
            _burstPlayer!!
        }

    internal var _audioburstLibrary: AudioburstLibrary? = null
    /**
     * Use [AudioburstLibrary] to request Audioburst content.
     */
    @JvmStatic
    public val audioburstLibrary: AudioburstLibrary
        get() = if (_audioburstLibrary == null) {
            error(INIT_ERROR_MESSAGE)
        } else {
            _audioburstLibrary!!
        }

    internal val isInjected: Boolean
        get() = _burstPlayer != null && _audioburstLibrary != null && this::mediaSessionConnection.isInitialized

    /**
     * This flag indicates whether library is initialized or not. When library is not initialized you
     * may experience that calling playback functions of the library will not take any effect.
     */
    @JvmStatic
    public val isInitialized: Boolean
        get() = isInjected && mediaSessionConnection.isConnected

    /**
     * Flag that indicates whether library should show Playback notification or not. While set to true
     * playback will happen in the Foreground Service.
     */
    @JvmStatic
    public var allowDisplayPlaybackNotification: Boolean = true
        set(value) {
            playbackNotificationController.shouldDisplayNotification.value = value
            field = value
        }

    /**
     * The function that initializes library. It should be used in the entry point of your application
     * (class that extends [Application]).
     *
     * @param context Context of the application.
     * @param applicationKey Key obtained from Audioburst Publishers (https://publishers.audioburst.com/).
     */
    @JvmStatic
    public fun init(context: Context, applicationKey: String) {
        if (!isInjected) {
            Injector.init(context, applicationKey)
            Injector.inject(this)
        }
        initMediaSession()
    }

    /**
     * The function that initializes library. It should be used in the entry point of your application
     * (class that extends [Application]). To be able to use this function you need to first put applicationKey
     * into AndroidManifest.xml under meta-data tag with "com.audioburst.applicationKey" key.
     *
     * @param context Context of the application.
     */
    @JvmStatic
    public fun init(context: Context) {
        val appInfo = context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
        val applicationKey = appInfo.metaData?.getString(APP_KEY_METADATA_KEY) ?: error(MISSING_METADATA_ERROR_MESSAGE)
        init(context, applicationKey)
    }

    private fun initMediaSession() {
        mediaSessionConnection.connect()
    }

    /**
     * This function lets you stop the player and release resources. It means that after calling this
     * function library is not initialized anymore and if you want to keep using it, you need to call
     * [init] function again.
     */
    @JvmStatic
    public fun stop() {
        mediaSessionConnection.disconnect()
    }

    /**
     * Use this function to get all of the available [PlaylistInfo].
     *
     * @return [Result.Data] when it was possible to get requested resource. In case there was a problem getting it
     * [Result.Error] will be returned with a proper ([LibraryError]).
     */
    @JvmStatic
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
    @JvmStatic
    public suspend fun getPersonalPlaylist(): Flow<Result<PendingPlaylist>> =
        audioburstLibrary.getPersonalPlaylist()

    /**
     * Use this function to get information about chosen [Playlist].
     *
     * @return [Result.Data] when it was possible to get requested resource. In case there was a problem getting it
     * [Result.Error] will be returned with a proper ([LibraryError]).
     */
    @JvmStatic
    public suspend fun getPlaylist(playlistInfo: PlaylistInfo): Result<Playlist> =
        audioburstLibrary.getPlaylist(playlistInfo)

    /**
     * You can use this function to pass previously recorded and converted to [ByteArray] search query.
     *
     * @return [Result.Data] when it was possible to get requested resource. In case there was a problem getting it
     * [Result.Error] will be returned with a proper ([LibraryError]).
     */
    @JvmStatic
    public suspend fun getPlaylist(byteArray: ByteArray): Result<Playlist> =
        audioburstLibrary.getPlaylist(byteArray)

    /**
     * You can use this function to pass search query and search for [Burst]s.
     *
     * Returns [Result.Data] when it was possible to get requested resource. When the API returned an empty list of [Burst]s
     * you will get [LibraryError.NoSearchResults]. In case there was a problem getting it [Result.Error] will be returned
     * with a proper error ([LibraryError]).
     */
    @JvmStatic
    public suspend fun search(query: String): Result<Playlist> =
        audioburstLibrary.search(query)

    /**
     * If you already have users in your app and you wouldn't like to register new one, you can use this function to
     * inform library what ABUserId it should use to communicate to the API. This function will return true if the given
     * ABUserId is correct and present in Audioburst database. Otherwise it will return false.
     *
     * @return [Result.Data] when it was possible to get requested resource. In case there was a problem getting it
     * [Result.Error] will be returned with a proper ([LibraryError]).
     */
    @JvmStatic
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
    @JvmStatic
    public fun ctaButtonClick(burstId: String) {
        audioburstLibrary.ctaButtonClick(burstId)
    }

    /**
     * By default Library will filter-out all already listened by the user [Burst]s. Use this function to change this behaviour.
     *
     * @param enabled Controls whether already listened [Burst]s should be filtered-out
     */
    @JvmStatic
    public fun filterListenedBursts(enabled: Boolean) {
        audioburstLibrary.filterListenedBursts(enabled)
    }

    /**
     * Loads a [Playlist].
     *
     * @param playlist The [Playlist] to load.
     * @param playWhenReady Whether playback should proceed when ready.
     */
    @JvmStatic
    public fun load(playlist: Playlist, playWhenReady: Boolean) {
        burstPlayer.load(playlist, playWhenReady)
    }

    /**
     * Trying to start playback if there is any [Playlist] ready.
     */
    @JvmStatic
    public fun play() {
        burstPlayer.play()
    }

    /**
     * Pauses playback.
     */
    @JvmStatic
    public fun pause() {
        burstPlayer.pause()
    }

    /**
     * Trying to move to the next [Burst].
     *
     * @return Whether it was possible to move to next.
     */
    @JvmStatic
    public fun next(): Boolean =
        burstPlayer.next()

    /**
     * Trying to move to the previous [Burst].
     *
     * @return Whether it was possible to move to previous.
     */
    @JvmStatic
    public fun previous(): Boolean =
        burstPlayer.previous()
}