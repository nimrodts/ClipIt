package com.nimroddayan.clipit.ui.motion

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sign

/**
 * Stiffness for a unit-mass spring that reaches its target in roughly [response] seconds
 * (Apple's "response" parameter). Compose's `spring()` takes stiffness, so this is the bridge.
 */
fun springStiffness(response: Float): Float {
    val omega = 2f * PI.toFloat() / response
    return omega * omega
}

/** Default deceleration for flick projection; matches normal scroll deceleration. */
const val DecelerationNormal = 0.998f

/**
 * Where a flick at [velocity] (px/s) would come to rest, measured from the release point.
 * Apple's projection from *Designing Fluid Interfaces*.
 */
fun projectedDistance(velocity: Float, decelerationRate: Float = DecelerationNormal): Float =
        (velocity / 1000f) * decelerationRate / (1f - decelerationRate)

/** Progressive resistance past a boundary: the further past, the less the element follows. */
fun rubberband(overshoot: Float, dimension: Float, constant: Float = 0.55f): Float =
        (overshoot * dimension * constant) / (dimension + constant * abs(overshoot))

/**
 * A swipe commits when its projected resting point passes [threshold] on the same side it was
 * dragged to, so a short fast flick commits and a flick back toward the start cancels.
 */
fun shouldCommitSwipe(offset: Float, velocity: Float, threshold: Float): Boolean {
    if (offset == 0f) return false
    val projected = offset + projectedDistance(velocity)
    return abs(projected) > threshold && sign(projected) == sign(offset)
}
