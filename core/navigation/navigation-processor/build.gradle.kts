plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ksp)
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(libs.symbol.processing.api)
    implementation(libs.kotlinpoet)

    implementation(project(":core:navigation:navigation-annotation"))
}
