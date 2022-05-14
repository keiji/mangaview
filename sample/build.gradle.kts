import org.jetbrains.kotlin.konan.properties.Properties

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("deploygate")
}

fun loadProperties(file: File): Properties {
    file.reader().use {
        return Properties().apply {
            load(it)
        }
    }
}

val compileSdkVersion: Int by rootProject.extra
val targetSdkVersion: Int by rootProject.extra
val minSdkVersion: Int by rootProject.extra
val kotlinVersion: String by rootProject.extra
val versionName: String by rootProject.extra
val versionCode: Int by rootProject.extra

android {
    compileSdk = this@Build_gradle.compileSdkVersion

    signingConfigs {
        val propFile = project.rootProject.file("release.properties")
        if (!propFile.exists()) {
            propFile.createNewFile()
        }

        val properties = loadProperties(propFile)
        val signStoreFilePath = properties.getProperty("signing.storeFilePath")
        val signStorePassword = properties.getProperty("signing.storePassword") ?: ""
        val signKeyAlias = properties.getProperty("signing.keyAlias") ?: ""
        val signKeyPassword = properties.getProperty("signing.keyPassword") ?: ""

        create("release") {
            if (signStoreFilePath == null) {
                return@create
            }

            storeFile = File(signStoreFilePath)
            storePassword = signStorePassword
            keyAlias = signKeyAlias
            keyPassword = signKeyPassword
        }
    }

    defaultConfig {
        applicationId = "jp.co.c_lis.mangaview.android"
        minSdk = minSdkVersion
        targetSdk = targetSdkVersion
        versionName = this@Build_gradle.versionName
        versionCode = this@Build_gradle.versionCode

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            val release = signingConfigs.getByName("release")
            if (release.storeFile != null) {
                signingConfig = release
            }
        }
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    val coroutinesVersion = "1.5.0"

    implementation(
        fileTree(
            mapOf(
                "dir" to "libs",
                "include" to listOf("*.aar", "*.jar"),
                "exclude" to listOf<String>()
            )
        )
    )

    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.appcompat:appcompat:1.4.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.3")
    implementation("androidx.viewpager2:viewpager2:1.1.0-beta01")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")

//    implementation("dev.keiji.mangaview:mangaview:$versionName")
    implementation(project(mapOf("path" to ":mangaview")))
}

deploygate {
    val propFile = project.rootProject.file("release.properties")
    if (!propFile.exists()) {
        propFile.createNewFile()
    }

    val properties = loadProperties(propFile)
    appOwnerName = properties.getProperty("deploygate.app_owner_name")
    apiToken = properties.getProperty("deploygate.api_token") ?: ""

    deployments {
        create("release") {
            val hash = "git rev-parse --short HEAD".executeAsCommand()
            message = "Sample build ${hash}"
        }
    }
}

fun String.executeAsCommand(): String = ProcessBuilder(this.split(" "))
    .directory(project.rootDir)
    .start()
    .apply { waitFor(10, TimeUnit.SECONDS) }
    .run {
        val error = errorStream.bufferedReader().readText().trim()
        if (error.isNotEmpty()) {
            return@run errorStream.bufferedReader().readText().trim()
        }
        return@run inputStream.bufferedReader().readText().trim()
    }
