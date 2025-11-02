package ch.scorpion.jabbah.draw.graphics

import ch.scorpion.jabbah.base.geom.Point2D

/**
 * Used as wrapper object in [Graphics2D] to abstract from platform-specific color gradients.
 */
data class RadialColorGradient(
    val center: Point2D,
    val radius: Double,
    val centerColor: Color,
    val perimeterColor: Color
) : Paint

/**
 * Caches [RadialColorGradients][RadialColorGradient] with fixed geometry for varying
 * center [Colors][Color].
 */
class RadialColorGradientCache(
    val center: Point2D,
    val radius: Double,
    val perimeterColor: Color
) {
    private val gradients = mutableMapOf<Color, RadialColorGradient>()

    fun forCenterColor(centerColor: Color): RadialColorGradient =
        gradients.getOrPut(centerColor) {
            RadialColorGradient(
                center,
                radius,
                centerColor,
                perimeterColor
            )
        }
}