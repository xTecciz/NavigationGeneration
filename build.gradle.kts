// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.android.library) apply false
    kotlin("jvm") version "2.0.21" apply false
}

buildscript {
    dependencies {
        classpath(kotlin("gradle-plugin", version = "2.0.21"))
    }
}