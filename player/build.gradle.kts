plugins {
    id(Dependencies.Plugins.sharedBuild)
    id(Dependencies.Plugins.libraryPublish)
    id(Dependencies.Plugins.explicitApiMode)
}

val archiveName by extra(Constants.Library.playerArchiveName)