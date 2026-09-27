/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * SPDX-FileCopyrightText: 2026 Bharat Dev Burman
 */

import dev.detekt.gradle.Detekt

/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * SPDX-FileCopyrightText: 2026 Bharat Dev Burman
 */

// Top-level build file where you can add configuration options common to all subprojects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.androidx.room) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.google.devtools.ksp) apply false
    alias(libs.plugins.google.dagger.hilt.android) apply false
    alias(libs.plugins.detekt) apply false
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0" apply false
}

tasks.register<Delete>("clean") {
    description = "Delete the build directory"
    delete(rootProject.layout.buildDirectory)
}
// Mockk uses ByteBuddy and the latter uses DynamicAgentLoading
subprojects {
    tasks.withType<Test>().configureEach {
        jvmArgs("-XX:+EnableDynamicAgentLoading")
    }

    tasks.withType<Detekt>().configureEach {
        reports {
            sarif.required.set(true)
            sarif.outputLocation.set(
                rootProject.layout.buildDirectory
                    .file(
                        "reports/detekt/${project.path.replace(":", "_").removePrefix("_")}.sarif"
                    )
            )
        }
    }
}
