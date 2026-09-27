/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * SPDX-FileCopyrightText: 2026 Bharat Dev Burman
 */

package dev.crazo7924.onlymusic

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication

/**
 * Custom [AndroidJUnitRunner] that initializes [HiltTestApplication] for instrumented UI tests.
 *
 * Referenced by its fully qualified class name in `:app/build.gradle.kts` (`testInstrumentationRunner`).
 */
@Suppress("unused")
class HiltTestRunner : AndroidJUnitRunner() {
    override fun newApplication(
        cl: ClassLoader?,
        className: String?,
        context: Context?
    ): Application {
        return super.newApplication(cl, HiltTestApplication::class.java.name, context)
    }
}
