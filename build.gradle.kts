// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.11.0" apply false
    id("org.jetbrains.kotlin.android") version "2.2.0" apply false
    alias(libs.plugins.compose.compiler) apply false
    id("com.android.library") version "8.11.0" apply false
    kotlin("jvm") version "2.1.21"
    kotlin("plugin.serialization") version "2.1.21"
}