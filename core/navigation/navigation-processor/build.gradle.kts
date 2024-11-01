plugins {
    kotlin("jvm") version "2.0.21"
    id("com.google.devtools.ksp") version "2.0.21-1.0.26"
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(libs.symbol.processing.api)
    implementation(libs.kotlinpoet)


    implementation(project(":core:navigation:navigation-annotation"))
}
