plugins {
    id(Dependencies.Plugins.sharedBuild)
    id(Dependencies.Plugins.libraryPublish)
    id(Dependencies.Plugins.explicitApiMode)
}

val project = Constants.Library.Controller
val archiveName by extra(project.archiveName)
val projectDescription by extra(project.projectDescription)

android {
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }
}

dependencies {
    api(project(Constants.Library.Core.projectName))
    implementation(Dependencies.Android.Ktx.core)
    coreLibraryDesugaring(Dependencies.JdkDesugar.desugar)
}