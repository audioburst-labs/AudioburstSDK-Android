import org.gradle.api.artifacts.dsl.DependencyHandler

object Constants {
    const val projectName = "AudioburstSDK-Android"
    const val projectVersion = "0.0.4"

    object Library {
        const val packageName = "com.audioburst"
        const val version = projectVersion

        object Core : Project {
            override val archiveName = "player-core"
            override val projectDescription = "AudioburstSDK is the SDK for Android that lets you access Audioburst content and add playback functionality to your app."
        }

        object Controller : Project {
            override val archiveName = "player-controller"
            override val projectDescription = "Use this library to build your own player that will let you play Audioburst's content."
        }

        interface Project {
            val archiveName: String
            val projectDescription: String

            val projectName: String
                get() = ":$archiveName"
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

    object Android {
        object Ktx {
            private const val version = "1.5.0"
            const val core = "androidx.core:core-ktx:$version"
        }
    }

    object Kotlin {
        const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion"
    }

    object MobileLibrary {
        private const val version = "0.0.+"
        const val library = "com.audioburst:mobile-library:$version"
    }

    object ExoPlayer {
        private const val version = "2.13.3"
        const val core = "com.google.android.exoplayer:exoplayer-core:$version"
        const val hls = "com.google.android.exoplayer:exoplayer-hls:$version"
        const val ui = "com.google.android.exoplayer:exoplayer-ui:$version"
        const val mediaSession = "com.google.android.exoplayer:extension-mediasession:$version"
        const val okHttp = "com.google.android.exoplayer:extension-okhttp:$version"
        const val testUtil = "com.google.android.exoplayer:exoplayer-testutils:$version"
        const val robolectricUtils = "com.google.android.exoplayer:exoplayer-robolectricutils:$version"
    }

    object OkHttp {
        private const val version = "3.12.11"
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

    object JdkDesugar {
        private const val version = "1.1.5"
        const val desugar = "com.android.tools:desugar_jdk_libs:$version"
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