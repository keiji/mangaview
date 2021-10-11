// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    val versionName = "1.1.1"
    val versionCode = 1110
    val kotlinVersion = "1.5.31"

    extra.apply {
        set("compileSdkVersion", 30)
        set("targetSdkVersion", 30)
        set("minSdkVersion", 15)
        set("buildToolsVersion", "30.0.2")
        set("kotlinVersion", kotlinVersion)

        set("versionName", versionName)
        set("versionCode", versionCode)
    }

    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.0.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${kotlinVersion}")

        classpath("com.deploygate:gradle:2.4.0")
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
