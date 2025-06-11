plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.vehicleqtt"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.vehicleqtt"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

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

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    // ✅ Enhanced packaging options for HiveMQ
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/LICENSE*"
            excludes += "/META-INF/NOTICE*"
            excludes += "/META-INF/INDEX.LIST"
            excludes += "/META-INF/DEPENDENCIES"
            excludes += "/META-INF/io.netty.versions.properties"
            excludes += "**/META-INF/maven/**"
            excludes += "**/META-INF/proguard/**"
            excludes += "META-INF/native-image/**"
            excludes += "META-INF/versions/**"
        }
        jniLibs {
            useLegacyPackaging = true
        }
    }
}

dependencies {
    // AndroidX + Jetpack Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // ✅ Lifecycle components
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")

    // ✅ Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // ✅ Google Maps SDK
    implementation("com.google.android.gms:play-services-maps:18.2.0")

    // ✅ Location Services
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // ✅ HiveMQ MQTT Client with exclusions
    implementation("com.hivemq:hivemq-mqtt-client:1.3.0") {
        exclude(group = "io.netty", module = "netty-buffer")
        exclude(group = "io.netty", module = "netty-codec")
        exclude(group = "io.netty", module = "netty-common")
        exclude(group = "io.netty", module = "netty-handler")
        exclude(group = "io.netty", module = "netty-resolver")
        exclude(group = "io.netty", module = "netty-transport")
        exclude(group = "io.netty", module = "netty-transport-native-epoll")
        exclude(group = "io.netty", module = "netty-transport-native-unix-common")
        exclude(group = "io.netty", module = "netty-resolver-dns")
        exclude(group = "io.netty", module = "netty-codec-http")
    }

    // ✅ Add compatible Netty versions for Android
    implementation("io.netty:netty-buffer:4.1.86.Final")
    implementation("io.netty:netty-codec:4.1.86.Final")
    implementation("io.netty:netty-common:4.1.86.Final")
    implementation("io.netty:netty-handler:4.1.86.Final")
    implementation("io.netty:netty-resolver:4.1.86.Final")
    implementation("io.netty:netty-transport:4.1.86.Final")

    // ✅ JSON handling
    implementation("com.google.code.gson:gson:2.10.1")

    // ✅ Network connectivity
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // ✅ Permissions handling
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.fragment:fragment-ktx:1.6.2")
}