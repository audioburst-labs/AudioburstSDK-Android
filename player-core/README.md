# AudioburstSDK core library module
A library that allows you to access Audioburst content and add playback functionality to your app.

## Get Started

This guide is a quick walkthrough to add `player-core` to an Android app. We recommend Android Studio as the development environment for building an app with the AudioburstSDK.

## Add `player-core` to your app

### Step 1. Add `player-core` dependency
![GitHub release](https://img.shields.io/github/v/release/audioburst-labs/AudioburstSDK-Android)

Add `player-core` to your project. To do this, add the following dependency in your app level `build.gradle` file:
```gradle
implementation 'com.audioburst:player-core:{latest-version}'
```

The library is built in Kotlin language and is using `Coroutines`, so to be able to support it you need to add the following configurations to your `android` script in the app level `build.config` file:

```gradle
android {
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}
```

### Step 2. Initialize `AudioburstPlayerCore` object
You should initialize `AudioburstPlayerCore` in the `onCreate` function of the class that extends `Application` in your app. Library is initialized until you call `stop` function.
```kotlin
AudioburstPlayerCore.init(
    context = context,
    applicationKey = "YOUR_API_KEY_HERE"
)
```

Another option to initialize the library is to put `applicationKey` into the AndroidManifest.xml file, under meta-data tag with "com.audioburst.applicationKey" name:
```xml
<meta-data
    android:name="com.audioburst.applicationKey"
    android:value="YOUR_API_KEY_HERE">
</meta-data>
```
Later you can call `init` method without passing "applicationKey":
```kotlin
AudioburstPlayerCore.init(context)
```

## Stop and release
The library exposes an ability to stop playback and release all resources. After you call this function library is not initialized anymore and if you want to start playback again you need to call `init` function again.
```kotlin
AudioburstPlayerCore.stop()
```

## Query library state
You can check whether library is initialized or not by accessing `isInitialized` flag. Please note that when library is not initialized you may experience that calling playback functions of the library will not take any effect.
```kotlin
AudioburstPlayerCore.isInitialized
```

## Legacy support
Use the library's 'setAudioburstUserID' function to keep a pre-existing Audioburst API User ID.
```kotlin
AudioburstPlayerCore.setAudioburstUserID("EXISTING_AUDIOBURST_API_USER_ID")
    .onData { isSuccess ->
        // If isSuccess is true, then you are ready to use existing Audioburst API User ID
    }
    .onError { error ->
        // Handle error
    }
```

### Step 3. Request Audioburst content

All the functions below are `suspending`, which means that you need to call them using a `CoroutineScope`.
Additionally, they return the `Result` object which can be either `Data` or `Error`.

The `AudioburstSDK` library is built on top of the `AudioburstMobileLibrary` library - [AudioburstMobileLibrary](https://github.com/audioburst-labs/AudioburstMobileLibrary). Some data structures used in this library are also exposed by `player-core`. Documentation for these can be found here: [wiki page](https://github.com/audioburst-labs/AudioburstMobileLibrary/wiki)

The library offers a few handy extension functions that make it easier to work with this type. Check the [Result](https://github.com/audioburst-labs/AudioburstMobileLibrary/blob/master/src/commonMain/kotlin/com/audioburst/library/models/Result.kt) class to learn more about it.

## Get all available playlists
```kotlin
AudioburstPlayerCore.getPlaylists()
    .onData { playlists ->
        // Display available playlists
    }
    .onError { error ->
        // Handle error
    }
```

## Get playlist information
```kotlin
AudioburstPlayerCore.getPlaylist(playlistItem)
    .onData { playlist ->
        // Build your playback queue by using list of Bursts
    }
    .onError { error ->
        // Handle error
    }
```

## Pass recorded PCM file
`AudioburstPlayerCore` is able to process raw audio files that contain a recorded request of what should be played. You can record a voice command stating what you would like to listen to and then upload it to your device and use AudioburstPlayerCore to get bursts on this topic.

```kotlin
AudioburstPlayerCore.getPlaylist(byteArray)
    .onData { playlist ->
        // Build your playback queue by using list of Bursts
    }
    .onError { error ->
        // Handle error
    }
```

The `getPlaylist` function accepts `ByteArray` as an argument. A request included in the PCM file will be processed and a playlist of the bursts will be returned.

## Search for a query
The Library exposes an ability to search for a text query. The response will either be a `Playlist` with the list of `Bursts` found OR a `NoSearchResults` error.
```kotlin
AudioburstPlayerCore.search(query)
    .onData { playlist ->
        // Build your playback queue by using list of Bursts
    }
    .onError { error ->
        // Handle error
    }
```

## Get Personalized Playlist using async
The library includes the capability to get a personalized playlist constructed according to a user’s preferences. In order to shorten the loading time of the personalized playlist, the library exposes the ability to "subscribe" to ongoing changes in the playlist. Subscribing enables notifications every time new `Burst`s are added to the playlist and the ability to check if the playlist is ready.

Please remember that your user needs to have at least one [Key](https://github.com/audioburst-labs/AudioburstMobileLibrary/blob/master/src/commonMain/kotlin/com/audioburst/library/models/UserPreferences.kt#L99) selected, otherwise `LibraryError.NoKeysSelected` will be returned.
```kotlin
audioburstLibrary
    .getPersonalPlaylist()
    .collect { result ->
        result
            .onData { pendingPlaylist ->
                if (pendingPlaylist.isReady) {
                    // Your playlist is ready
                } else {
                    // Your playlist is still being prepared
                }
            }
            .onError { error ->
                // Handle error
            }
    }
```

## Use Cta Data
`Burst` class exposes nullable `CtaData`, which you can use to show a CTA (Call to action) button which prompts the user to an immediate response.
The CtaData, when available, provides the text to be shown on the button (`buttonText`) and the link (`url`) to open in the browser upon clicking the button.
When the user clicks this button, you should call the following function to inform the library about this:
```kotlin
audioburstLibrary.ctaButtonClick(burstId)
```

## Filter out listened Bursts
By default, library will filter-out all Bursts that the user has already listened to. Use `filterListenedBursts` function to change this behaviour.
```kotlin
audioburstLibrary.filterListenedBursts(isEnabled)
```

### Step 4. Inform library about current playback state

### Step 3. Load playlist and start playback

`AudioburstPlayerCore` will let you easily control the playback of the `Playlist`.

## Load playlist
When you already have a `Playlist` that you would like to play, you can use `load` function to prepare a playback:
```kotlin
AudioburstPlayerCore.load(
    playlist = playlist,
    playWhenReady = playWhenReady,
)
```
`playWhenReady` flag controls whether playback should start automatically or not.

## Control playback
`AudioburstPlayerCore` exposes a set of simple methods that will let you control playback state:
- `play` - starts playback if a `Playlist` is ready,
- `pause` - pauses playback,
- `next` - moves to the next `Burst` when possible.
- `previous` - moves to the previous `Burst` when possible.

## Use `BurstPlayer`
The `BurstPlayer` interface notifies every time the playback state changes. For example, it sends information that a user clicked the Play button, or information about the content that is playing. It can be obtained the following way:
```kotlin
val burstPlayer = AudioburstPlayerCore.burstPlayer
```

[BurstPlayer][BurstPlayer] exposes an ability to subscribe to updates about [PlaybackState][PlaybackState], [NowPlaying][NowPlaying], [AdState][AdState] and [PlaybackTime][PlaybackTime]. It also provides the ability to control playback via functions like the ones described above. It also uses methods like, for example, to switch from playing `Burst` content to `BurstSource` content and much more.

You can learn more about features of this class [here][BurstPlayer].

## Privacy Policy
[Privacy Policy](https://audioburst.com/privacy)

## Terms of Service
[Terms of Service](https://audioburst.com/audioburst-publisher-terms)

[BurstPlayer]: https://github.com/audioburst-labs/AudioburstSDK-Android/blob/master/player-core/src/main/java/com/audioburst/player/core/media/BurstPlayer.kt
[AdState]: https://github.com/audioburst-labs/AudioburstSDK-Android/blob/master/player-core/src/main/java/com/audioburst/player/core/models/AdState.kt
[NowPlaying]: https://github.com/audioburst-labs/AudioburstSDK-Android/blob/master/player-core/src/main/java/com/audioburst/player/core/models/NowPlaying.kt
[PlaybackState]: https://github.com/audioburst-labs/AudioburstSDK-Android/blob/master/player-core/src/main/java/com/audioburst/player/core/models/PlaybackState.kt
[PlaybackTime]: https://github.com/audioburst-labs/AudioburstSDK-Android/blob/master/player-core/src/main/java/com/audioburst/player/core/models/PlaybackTime.kt