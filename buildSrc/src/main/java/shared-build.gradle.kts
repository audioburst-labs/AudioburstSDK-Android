plugins {
    id("com.android.library")
    id("kotlin-android")
}

group = Constants.Library.packageName
version = Constants.Library.version

android {
    compileSdkVersion(30)

    defaultConfig {
        minSdkVersion(22)
        targetSdkVersion(30)
        versionCode = 1
        versionName = Constants.Library.version

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFile("proguard-rules.pro")
        buildConfigField("String", "LIBRARY_VERSION", "\"${Constants.Library.version}\"")
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