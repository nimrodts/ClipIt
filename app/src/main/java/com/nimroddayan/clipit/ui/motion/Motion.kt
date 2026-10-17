package com.nimroddayan.clipit.ui.motion

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.spring

/**
 * Springs described the way Apple does: damping ratio (1 = no overshoot) and response (seconds).
 * Values were tuned in the redesign prototype.
 */
object Motion {
    /** Default for anything not driven by a gesture: critically damped, no bounce. */
    fun <T> standard(response: Float = 0.38f): FiniteAnimationSpec<T> =
            spring(dampingRatio = 1f, stiffness = springStiffness(response))

    /** After a flick or swipe release: the gesture carried momentum, so allow a little bounce. */
    fun <T> momentum(response: Float = 0.38f): FiniteAnimationSpec<T> =
            spring(dampingRatio = 0.8f, stiffness = springStiffness(response))

    /** Quick, settled changes such as a row collapsing or content fading. */
    fun <T> quick(): FiniteAnimationSpec<T> = spring(dampingRatio = 1f, stiffness = springStiffness(0.3f))

    /** Card ↔ sheet morph. */
    fun <T> sheet(): FiniteAnimationSpec<T> = standard(0.38f)
}
