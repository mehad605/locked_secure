import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.detekt)
    alias(libs.plugins.spotless)
}

android {
    namespace = "com.example.lockedsecure"
    compileSdk = 36

    // Signing configuration: local app/keystore.properties or CI environment variables
    val keystorePropertiesFile = rootProject.file("app/keystore.properties")
    val keystoreProperties = Properties()
    var hasSigning = false

    if (keystorePropertiesFile.exists()) {
        FileInputStream(keystorePropertiesFile).use { keystoreProperties.load(it) }
        hasSigning = true
    } else {
        val envFile = System.getenv("ANDROID_SIGNING_KEY_FILE")
        val envStorePass = System.getenv("ANDROID_SIGNING_STORE_PASSWORD")
        val envAlias = System.getenv("ANDROID_SIGNING_KEY_ALIAS")
        val envKeyPass = System.getenv("ANDROID_SIGNING_KEY_PASSWORD")

        if (
            !envFile.isNullOrBlank() &&
            !envStorePass.isNullOrBlank() &&
            !envAlias.isNullOrBlank() &&
            !envKeyPass.isNullOrBlank()
        ) {
            keystoreProperties["storeFile"] = envFile
            keystoreProperties["storePassword"] = envStorePass
            keystoreProperties["keyAlias"] = envAlias
            keystoreProperties["keyPassword"] = envKeyPass
            hasSigning = true
        }
    }

    val envVersionName = System.getenv("APP_VERSION_NAME")
    val envVersionCode = System.getenv("APP_VERSION_CODE")?.toIntOrNull()

    defaultConfig {
        applicationId = "com.example.lockedsecure"
        minSdk = 24
        targetSdk = 36
        versionCode = envVersionCode ?: 1
        versionName = envVersionName ?: "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    androidResources {
        // Keep only English strings; prevents transitive/platform locale bloat in resources.arsc
        localeFilters += listOf("en")
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }

    signingConfigs {
        if (hasSigning) {
            create("release") {
                val storePath = keystoreProperties["storeFile"] as String
                storeFile = file(storePath)
                storePassword = keystoreProperties["storePassword"] as String
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
            }
        }
    }

    buildTypes {
        release {
            if (hasSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
            isMinifyEnabled = true
            isShrinkResources = true
            vcsInfo.include = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
        debug {
            if (hasSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        aidl = false
        buildConfig = false
        compose = false
        resValues = false
        shaders = false
    }

    // Omit Play dependency block for deterministic, smaller release APKs
    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }

    lint {
        abortOnError = true
        warningsAsErrors = true
        checkReleaseBuilds = true
        lintConfig = file("lint.xml")
    }

    packaging {
        resources {
            excludes +=
                setOf(
                    "/META-INF/{AL2.0,LGPL2.1}",
                    "/META-INF/*.version",
                    "/META-INF/*.kotlin_module",
                    "DebugProbesKt.bin",
                    "kotlin/**",
                )
        }
    }
}

kotlin {
    jvmToolchain(17)
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom(rootProject.file("config/detekt/detekt.yml"))
    source.setFrom(files("src/main/java", "src/test/java"))
}

spotless {
    kotlin {
        target("src/**/*.kt")
        ktlint("1.5.0")
            .setEditorConfigPath(rootProject.file(".editorconfig"))
    }
    kotlinGradle {
        target("*.gradle.kts")
        ktlint("1.5.0")
            .setEditorConfigPath(rootProject.file(".editorconfig"))
    }
}

dependencies {
    // Zero third-party runtime dependencies: LockedSecure uses Android platform APIs only.

    // JVM unit & Robolectric characterization tests
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
}
