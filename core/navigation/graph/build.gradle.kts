plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.graph"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
        consumerProguardFiles("consumer-rules.pro")
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(libs.androidx.navigation.common.ktx)
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(project(":core:navigation:feature-launcher-api"))
    ksp(project(":core:navigation:navigation-processor"))
    implementation(project(":core:navigation:navigation-annotation"))
    implementation(project(":feature:feature-a"))
    implementation(project(":feature:feature-b"))
    implementation(project(":feature:feature-c"))
}