plugins {
    id(Dependencies.Plugins.sharedBuild)
    id(Dependencies.Plugins.libraryPublish)
    id(Dependencies.Plugins.explicitApiMode)
}

val project = Constants.Library.Core
val archiveName by extra(project.archiveName)
val projectDescription by extra(project.projectDescription)

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
    api(Dependencies.MobileLibrary.library)
    testImplementation(Dependencies.OkHttp.mockWebServer)
}