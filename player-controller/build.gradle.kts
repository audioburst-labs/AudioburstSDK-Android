plugins {
    id(Dependencies.Plugins.sharedBuild)
    id(Dependencies.Plugins.libraryPublish)
    id(Dependencies.Plugins.explicitApiMode)
}

val project = Constants.Library.Controller
val archiveName by extra(project.archiveName)
val projectDescription by extra(project.projectDescription)

dependencies {
    api(project(Constants.Library.Core.projectName))
}