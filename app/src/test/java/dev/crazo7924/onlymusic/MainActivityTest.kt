package dev.crazo7924.onlymusic

import androidx.lifecycle.Lifecycle
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class MainActivityTest {
    @Test
    fun activity_lifecycle_resumed_after_setup() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).setup().get()
        assertNotNull(activity)
        assertEquals(Lifecycle.State.RESUMED, activity.lifecycle.currentState)
    }
}
