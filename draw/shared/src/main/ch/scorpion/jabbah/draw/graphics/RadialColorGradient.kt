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
    val backgroundColor: Color
) {
    private val cache = mutableMapOf<Int, Paint>()

    private val backgroundWithAlphaColor = backgroundColor.withAlpha(0)

    fun forFactoredColorGradient(gradient: ColorGradient, factor: Float): Paint {
        val factorInt = (factor * 100).toInt()
        val alpha = (factor * 255).toInt()
        return cache.getOrPut(factorInt) {
            val centerColor = gradient.at(1.0f)
            RadialColorGradient(
                center,
                radius,
                centerColor.withAlpha(alpha),
                backgroundWithAlphaColor
            )
        }
    }
}