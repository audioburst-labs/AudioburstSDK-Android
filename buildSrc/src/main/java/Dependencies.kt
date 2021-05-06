object Constants {
    const val projectName = "AudioburstSDK-Android"
    const val projectVersion = "0.0.1"

    object Library {
        const val packageName = "com.audioburst"
        const val version = projectVersion
        const val playerArchiveName = "player"
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
}