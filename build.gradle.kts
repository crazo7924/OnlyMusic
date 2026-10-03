/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * SPDX-FileCopyrightText: 2026 Bharat Dev Burman
 */

import dev.detekt.gradle.Detekt
import dev.detekt.gradle.report.ReportMergeTask

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

val mergeTask = tasks.register<ReportMergeTask>("mergeReports") {
    description = "Merge the generated SARIF reports by Detekt"
    output.set(rootProject.layout.buildDirectory.file("reports/detekt/merged.sarif"))
}

subprojects {
    // Configure unit tests for JDK 21+ dynamic agent loading (MockK/ByteBuddy)
    tasks.withType<Test>().configureEach {
        jvmArgs("-XX:+EnableDynamicAgentLoading")
    }

    // Safely configure subprojects that apply the Detekt plugin
    pluginManager.withPlugin("dev.detekt") {
        val detektTasks = tasks.withType<Detekt>()

        detektTasks.configureEach {
            config.setFrom(rootProject.file("detekt.yml"))
            reports {
                sarif.required.set(true)
            }
            finalizedBy(mergeTask)
        }
        // Wire SARIF reports into merge task lazily without eager task realization
        mergeTask.configure {
            input.from(detektTasks.map { it.reports.sarif.outputLocation })
        }
    }
}