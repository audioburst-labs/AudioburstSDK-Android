import org.gradle.api.artifacts.dsl.DependencyHandler

object Constants {
    const val projectName = "AudioburstSDK-Android"
    const val projectVersion = "0.0.6"

    object Library {
        const val packageName = "com.audioburst"
        const val version = projectVersion

        object Core {
            const val archiveName = "player-core"
            const val projectDescription = "AudioburstSDK is the SDK for Android that lets you access Audioburst content and add playback functionality to your app."
        }
    }
}

object Dependencies {
    const val kotlinVersion = "1.5.0"

    object Plugins {
        const val sharedBuild = "shared-build"
        const val libraryPublish = "library-publish"
        const val explicitApiMode = "explicit-api-mode"
        const val kotlinGradle = "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion"
        const val buildGradle = "com.android.tools.build:gradle:4.2.0"
    }

    object Kotlin {
        const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion"
    }

    object MobileLibrary {
        private const val version = "0.0.+"
        const val library = "com.audioburst:mobile-library:$version"
    }

    object ExoPlayer {
        private const val version = "2.16.1"
        const val core = "com.google.android.exoplayer:exoplayer-core:$version"
        const val hls = "com.google.android.exoplayer:exoplayer-hls:$version"
        const val ui = "com.google.android.exoplayer:exoplayer-ui:$version"
        const val mediaSession = "com.google.android.exoplayer:extension-mediasession:$version"
        const val okHttp = "com.google.android.exoplayer:extension-okhttp:$version"
    }

    object OkHttp {
        private const val version = "4.9.3"
        val mockWebServer = "com.squareup.okhttp3:mockwebserver:$version"
    }

    object Test {

        object Junit {
            private const val version = "4.13.2"
            const val junit = "junit:junit:$version"
        }

        object Android {
            private const val version = "1.2.0"
            const val runner = "androidx.test:runner:$version"
            const val core = "androidx.test:core:$version"
            const val rules = "androidx.test:rules:$version"
        }
    }
}

private fun DependencyHandler.implementation(dependency: String) {
    add("implementation", dependency)
}

fun DependencyHandler.androidTestImplementation(dependency: String) {
    add("androidTestImplementation", dependency)
}

fun DependencyHandler.exoPlayerDependencies() {
    implementation(Dependencies.ExoPlayer.core)
    implementation(Dependencies.ExoPlayer.hls)
    implementation(Dependencies.ExoPlayer.ui)
    implementation(Dependencies.ExoPlayer.mediaSession)
    implementation(Dependencies.ExoPlayer.okHttp)
}