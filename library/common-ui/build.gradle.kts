plugins {
    alias(libs.plugins.android.library)
}

val srcDir = arrayOf (
    "safemode",
    "dashboard"
)

android {
    namespace = "com.sevtinge.hyperceiler.ui"
    compileSdk = 36

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    defaultConfig {
        minSdk = 34

        buildConfigField("String", "APP_MODULE_ID", "\"com.harry.hyperhand\"")
    }

    buildFeatures {
        buildConfig = true
    }

    sourceSets {
        getByName("main") {
            //java.srcDir("java")
            java.srcDirs("java/main/src")
            res.srcDirs("java/main/res")
            manifest.srcFile("java/AndroidManifest.xml")

            srcDir.forEach {
                java.srcDirs("java/$it/src")
                res.srcDirs("java/$it/res")
            }
        }
    }

    buildTypes {
        release {
            consumerProguardFiles(libs.versions.proguard.rules.get())
        }
        create("beta") {
            consumerProguardFiles(libs.versions.proguard.rules.get())
        }
        create("canary") {
            consumerProguardFiles(libs.versions.proguard.rules.get())
        }
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

dependencies {

    api(libs.miuix.animation)
    api(libs.miuix.appcompat)
    api(libs.miuix.basewidget)
    api(libs.miuix.bottomsheet)
    api(libs.miuix.cardview)
    api(libs.miuix.core)
    api(libs.miuix.folme)
    api(libs.miuix.navigator)
    api(libs.miuix.nestedheader)
    api(libs.miuix.pickerwidget)
    api(libs.miuix.preference)
    api(libs.miuix.recyclerview)
    api(libs.miuix.springback)
    api(libs.miuix.theme)
    api(libs.miuix.viewpager)
    api(libs.miuix.transition)

    api(projects.library.hook)
}
