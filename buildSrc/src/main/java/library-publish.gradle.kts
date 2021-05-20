import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("maven-publish")
    id("signing")
}

val sources by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(android.sourceSets["main"].java.srcDirs)
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("aar") {
                val libraryVersion = Constants.Library.version
                val projectGroup = Constants.Library.packageName
                val archiveName: String by extra
                val projectDescription: String by extra

                groupId = projectGroup
                artifactId = archiveName
                version = libraryVersion

                artifact("$buildDir/outputs/aar/$archiveName-release.aar")
                artifact(sources.get())

                pom {
                    name.set(artifactId)
                    description.set(projectDescription)
                    url.set("https://github.com/audioburst-labs/AudioburstSDK-Android")
                    licenses {
                        license {
                            name.set("Terms of Service")
                            url.set("https://audioburst.com/audioburst-publisher-terms")
                        }
                    }
                    developers {
                        developer {
                            id.set("Kamil-H")
                            name.set("Kamil Halko")
                            email.set("kamil@audioburst.com")
                        }
                    }
                    scm {
                        url.set("https://github.com/audioburst-labs/AudioburstSDK-Android/tree/master")
                    }
                    withXml {
                        val dependenciesNode = asNode().appendNode("dependencies")
                        configurations.releaseImplementation.get().allDependencies.forEach {
                            dependenciesNode.appendNode("dependency").apply {
                                appendNode("groupId", it.group)
                                appendNode("artifactId", it.name)
                                appendNode("version", it.version)
                            }
                        }
                    }
                    repositories {
                        maven {
                            url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
                            credentials {
                                username = gradleLocalProperties(rootDir).getProperty("ossrhUsername")
                                password = gradleLocalProperties(rootDir).getProperty("ossrhPassword")
                            }
                        }
                    }
                }
            }
        }
    }
}


ext["signing.keyId"] = gradleLocalProperties(rootDir).getProperty("signing.keyId")
ext["signing.secretKeyRingFile"] = gradleLocalProperties(rootDir).getProperty("signing.secretKeyRingFile")
ext["signing.password"] = gradleLocalProperties(rootDir).getProperty("signing.password")

signing {
    sign(publishing.publications)
}

val assembleReleaseAndPublishToMavenRepository by tasks.registering {
    dependsOn("assembleRelease")
    dependsOn("generatePomFileForAarPublication")
    dependsOn("publishAarPublicationToMavenRepository")
    tasks.findByName("generatePomFileForAarPublication")?.mustRunAfter("assembleRelease")
    tasks.findByName("publishAarPublicationToMavenRepository")?.mustRunAfter("generatePomFileForAarPublication")
}

val assembleReleaseAndPublishToMavenLocal by tasks.registering {
    dependsOn("assembleRelease")
    dependsOn("generatePomFileForAarPublication")
    dependsOn("publishAarPublicationToMavenLocal")
    tasks.findByName("generatePomFileForAarPublication")?.mustRunAfter("assembleRelease")
    tasks.findByName("publishAarPublicationToMavenLocal")?.mustRunAfter("generatePomFileForAarPublication")
}