plugins {
    kotlin("jvm") version libs.versions.kotlin
    kotlin("plugin.serialization") version libs.versions.kotlin
    alias(libs.plugins.ktlint) version libs.versions.ktlint
}

group = "com.sigma67"
version = "1.11.0"
description = "Unofficial API for YouTube Music"

dependencies {
    // HTTP client
    implementation(libs.okhttp)

    // JSON serialization
    implementation(libs.kotlinx.serialization.json)

    // Logging
    implementation(libs.kotlin.logging)
    implementation(libs.logback.classic)

    // Internationalization
    implementation(libs.commons.text)

    // Testing
    testImplementation(kotlin("test"))
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.mockk)
}

kotlin {
    jvmToolchain(17)

    compilerOptions {
        verbose.set(false)
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

java {
    withSourcesJar()
}
