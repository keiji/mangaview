plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("plugin.parcelize")
    id("maven-publish")
    id("signing")
}

val compileSdkVersion: Int by rootProject.extra
val targetSdkVersion: Int by rootProject.extra
val minSdkVersion: Int by rootProject.extra
val kotlinVersion: String by rootProject.extra

val versionName: String by rootProject.extra

android {
    compileSdk = this@Build_gradle.compileSdkVersion

    defaultConfig {
        minSdk = minSdkVersion
        targetSdk = targetSdkVersion

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

tasks.create("javadocJar", Jar::class) {
    archiveClassifier.set("javadoc")
    from(File(buildDir, "docs/javadoc"))
}

tasks.create("sourcesJar", Jar::class) {
    dependsOn("assemble")
    archiveClassifier.set("sources")
    from(android.sourceSets.getByName("main").java.srcDirs)
}

dependencies {
    implementation(
        fileTree(
            mapOf(
                "dir" to "libs",
                "include" to listOf("*.aar", "*.jar"),
                "exclude" to listOf<String>()
            )
        )
    )

    implementation("org.jetbrains.kotlin:kotlin-stdlib:${kotlinVersion}")
    implementation("androidx.core:core-ktx:1.7.0")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:4.0.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
}

val libraryName = "mangaview"
val groupIdValue = "dev.keiji.mangaview"

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = groupIdValue
            artifactId = libraryName
            version = versionName

            from(components.findByName("release"))
            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])

            pom {
                name.set("MangaView")
                description.set("An ImageView for reading manga(comics) for Android.")
                url.set("https://github.com/keiji/mangaview")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("keiji")
                        name.set("ARIYAMA Keiji")
                        email.set("keiji.ariyama@gmail.com")
                    }
                }
                scm {
                    connection.set("https://github.com/keiji/mangaview.git")
                    developerConnection.set("https://github.com/keiji/mangaview.git")
                    url.set("https://github.com/keiji/mangaview")
                }
            }
        }
    }

    repositories {
        maven {
            val releasesRepoUrl =
                "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
            val snapshotsRepoUrl =
                "https://s01.oss.sonatype.org/content/repositories/snapshots/"
            val selectedUrl = if (versionName.endsWith("-SNAPSHOT")) {
                snapshotsRepoUrl
            } else {
                releasesRepoUrl
            }
            setUrl(selectedUrl)

            val sonatypeUsername: String? by project
            val sonatypePassword: String? by project
            credentials {
                username = sonatypeUsername
                password = sonatypePassword
            }
        }
    }
}

signing {
    if (!project.properties.containsKey("signing.password")) {
        print("signing.password must be set.")
        return@signing
    }

    val signingPassword = project.properties["signing.password"] as String?
    if (signingPassword.isNullOrEmpty()) {
        print("signing.password must be set.")
        return@signing
    }

    sign(publishing.publications["maven"])
}
