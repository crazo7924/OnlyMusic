/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * SPDX-FileCopyrightText: 2026 Bharat Dev Burman
 */

import dev.detekt.gradle.Detekt

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.androidx.room) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.google.devtools.ksp) apply false
    alias(libs.plugins.google.dagger.hilt.android) apply false
    alias(libs.plugins.detekt) apply false
}

tasks.register<Delete>("clean") {
    description = "Delete the build directory"
    delete(rootProject.layout.buildDirectory)
}

subprojects {
    // Configure unit tests for JDK 21+ dynamic agent loading (MockK/ByteBuddy)
    tasks.withType<Test>().configureEach {
        jvmArgs("-XX:+EnableDynamicAgentLoading")
    }

    // Use the shared Detekt configuration for every subproject that applies Detekt.
    pluginManager.withPlugin("dev.detekt") {
        tasks.withType<Detekt>().configureEach {
            config.setFrom(rootProject.file("detekt.yml"))
        }
    }
}
