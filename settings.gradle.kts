@file:Suppress("UnstableApiUsage")
var gprUser = System.getenv("GIT_ACTOR") ?:""
var gprKey = System.getenv("GIT_TOKEN") ?: ""

val gprInfoFile = File(rootProject.projectDir, "signing.properties")

if (gprUser.isEmpty() || gprKey.isEmpty()) {
    if (gprInfoFile.exists()) {
        val gprInfo = java.util.Properties().apply {
            gprInfoFile.inputStream().use { load(it) }
        }
        gprUser = gprInfo.getProperty("gpr.user") ?: ""
        gprKey = gprInfo.getProperty("gpr.key") ?: ""
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        mavenLocal()
        if (gprUser.isNotEmpty() && gprKey.isNotEmpty()) {
            maven {
                url = uri("https://maven.pkg.github.com/ReChronoRain/HyperCeiler")
                credentials {
                    username = gprUser
                    password = gprKey
                }
            }
        }
        maven("https://jitpack.io")
        maven("https://api.xposed.info")
    }
}

rootProject.name = "HyperCeilerModded"
include("app")
include(":library:common-ui", ":library:hook", "library:processor", "library:hidden-api")
