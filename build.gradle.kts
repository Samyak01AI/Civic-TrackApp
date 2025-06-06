// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.google.gms.google.services) apply false
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin) apply false
}
buildscript {
    dependencies {
        // Top-level build file where you can add configuration options common to all sub-projects/modules.
        buildscript {

            classpath("com.google.gms:google-services:4.4.0")
            classpath("com.android.tools.build:gradle:8.1.2")
            classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.7.7")
        }
    }
}
