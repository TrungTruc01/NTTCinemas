plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.nttcinemas"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.nttcinemas"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        multiDexEnabled = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    configurations.all {
        resolutionStrategy {
            force ("androidx.coordinatorlayout:coordinatorlayout:1.1.0") // Chỉ định phiên bản duy nhất
        }
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.palette.ktx)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.storage.ktx)
    implementation(fileTree(mapOf(
        "dir" to "E:\\DH_TDM\\Nam4\\HK1\\PTUD\\LT\\BAOCAOCUOIKI\\ZaloPay",
        "include" to listOf("*.aar", "*.jar"),
        "exclude" to listOf("")
    )))
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.firebase.database)
    implementation ("com.github.bumptech.glide:glide:4.15.1")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.15.1")
    implementation ("com.github.ismaeldivita:chip-navigation-bar:1.4.0")
    implementation ("com.github.Dimezis:BlurView:version-2.0.3")
    implementation ("androidx.palette:palette:1.0.0")
    implementation ("com.google.firebase:firebase-auth:23.1.0")
    implementation ("com.github.chrisbanes:PhotoView:2.3.0")
    implementation ("com.squareup.okhttp3:okhttp:4.11.0")
    implementation ("org.json:json:20210307") // For JSON parsing
    implementation ("com.google.firebase:firebase-firestore:25.1.1")
    implementation ("com.google.firebase:firebase-core:21.1.1")
    implementation("commons-codec:commons-codec:1.15")
    implementation ("com.google.android.gms:play-services-auth:21.3.0")
    implementation ("com.google.android.gms:play-services-identity:18.1.0")
    implementation ("androidx.coordinatorlayout:coordinatorlayout:1.2.0")
    implementation("androidx.fragment:fragment-ktx:1.8.5")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.5")
    implementation ("androidx.drawerlayout:drawerlayout:1.2.0")
    implementation ("org.jsoup:jsoup:1.15.3")
    implementation ("com.opencsv:opencsv:5.5.2")
    implementation ("com.google.code.gson:gson:2.10.1")
    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation ("org.apache.poi:poi:5.2.3")      // Thư viện chính của Apache POI
    implementation ("org.apache.poi:poi-ooxml:5.2.3") // Hỗ trợ định dạng file .xlsx
    implementation ("org.apache.commons:commons-collections4:4.4") // Yêu cầu bởi POI
    implementation ("androidx.multidex:multidex:2.0.1")
}