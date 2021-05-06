plugins {
    `kotlin-dsl`
    `kotlin-dsl-precompiled-script-plugins`
}

gradlePlugin {
    plugins {
        register("explicit-api-mode") {
            id = "explicit-api-mode"
            implementationClass = "ExplicitApiModePlugin"
        }
    }
}

buildscript {
    repositories {
        mavenCentral()
        google()
    }

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.0")
    }
}

repositories {
    mavenCentral()
    google()
}

dependencies {
    implementation("com.android.tools.build:gradle:4.2.0")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.0")
}