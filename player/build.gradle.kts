plugins {
    id(Dependencies.Plugins.sharedBuild)
    id(Dependencies.Plugins.libraryPublish)
    id(Dependencies.Plugins.explicitApiMode)
}

val archiveName by extra(Constants.Library.playerArchiveName)

android {
    val sharedTestDir = "src/sharedTest/java"
    sourceSets.getByName("test") {
        java.srcDir(sharedTestDir)
    }
    sourceSets.getByName("androidTest") {
        java.srcDir(sharedTestDir)
    }
}

dependencies {
    exoPlayerDependencies()
    implementation(Dependencies.MobileLibrary.library)
    testImplementation(Dependencies.OkHttp.mockWebServer)
}