plugins {
    id("com.android.library")
    id("kotlin-android")
}

android {
    compileSdkVersion(32)

    defaultConfig {
        minSdkVersion(22)
        targetSdkVersion(32)
        versionCode = 1
        versionName = Constants.Library.version

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFile("proguard-rules.pro")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(Dependencies.Kotlin.stdlib)
    implementation(Dependencies.Test.Junit.junit)
    add("androidTestImplementation", Dependencies.Test.Android.runner)
}