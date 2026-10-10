package com.nimroddayan.clipit.ui.motion

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PhysicsTest {

    @Test
    fun `stiffness for response matches (2π divided by response) squared`() {
        // response 0.38s -> (2π / 0.38)^2 ≈ 273.4
        assertEquals(273.4f, springStiffness(0.38f), 0.5f)
    }

    @Test
    fun `shorter response gives a stiffer spring`() {
        assertTrue(springStiffness(0.2f) > springStiffness(0.5f))
    }

    @Test
    fun `projection of zero velocity is zero`() {
        assertEquals(0f, projectedDistance(0f), 0.0001f)
    }

    @Test
    fun `projection uses Apple's exponential decay form`() {
        // (1000 / 1000) * 0.998 / (1 - 0.998) = 499
        assertEquals(499f, projectedDistance(1000f, 0.998f), 0.5f)
    }

    @Test
    fun `projection keeps the sign of the velocity`() {
        assertTrue(projectedDistance(-800f) < 0f)
    }

    @Test
    fun `snappier deceleration projects a shorter distance`() {
        assertTrue(projectedDistance(1000f, 0.99f) < projectedDistance(1000f, 0.998f))
    }

    @Test
    fun `rubberband of zero overshoot is zero`() {
        assertEquals(0f, rubberband(0f, 400f), 0.0001f)
    }

    @Test
    fun `rubberband gives less than the raw overshoot`() {
        val result = rubberband(200f, 400f)
        assertTrue(result > 0f)
        assertTrue(result < 200f)
    }

    @Test
    fun `rubberband never exceeds the dimension`() {
        assertTrue(rubberband(1_000_000f, 400f) < 400f)
    }

    @Test
    fun `rubberband is symmetric for negative overshoot`() {
        assertEquals(-rubberband(120f, 400f), rubberband(-120f, 400f), 0.0001f)
    }

    @Test
    fun `swipe commits when the projected position passes the threshold`() {
        // Released at 60px, below a 100px threshold, but flicked at 600px/s
        assertTrue(shouldCommitSwipe(offset = 60f, velocity = 600f, threshold = 100f))
    }

    @Test
    fun `swipe does not commit when flicked back toward the start`() {
        assertTrue(!shouldCommitSwipe(offset = 140f, velocity = -900f, threshold = 100f))
    }

    @Test
    fun `swipe does not commit for a slow release short of the threshold`() {
        assertTrue(!shouldCommitSwipe(offset = 60f, velocity = 0f, threshold = 100f))
    }

    @Test
    fun `swipe commits for a slow release past the threshold`() {
        assertTrue(shouldCommitSwipe(offset = -140f, velocity = 0f, threshold = 100f))
    }
}
