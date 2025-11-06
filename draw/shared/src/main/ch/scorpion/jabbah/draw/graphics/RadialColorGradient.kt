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

class MultiRadialColorGradient(
    val center: Point2D,
    val radius: Double,
    val fractions: FloatArray,
    val colors: Array<Color>
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

    private val fractions = FloatArray(3).also {
        it[0] = 0f
        it[1] = 0.6f
        it[2] = 1f
    }

    fun forFactoredColorGradient(gradient: ColorGradient, factor: Float): Paint {
        val factorInt = (factor * 100).toInt()
        val alpha1 = (factor * 255).toInt()

        // Variant with very bright center
        /*
        val alpha2 = (factor * 0.90 * 255).toInt()
        return cache.getOrPut(factorInt) {
            val centerColor = gradient.at(1.0f)
            MultiRadialColorGradient(
                center,
                radius,
                fractions,
                arrayOf(
                    centerColor.withAlpha(alpha1),
                    centerColor.withAlpha(alpha2),
                    backgroundWithAlphaColor
                )
            )
        }
        */
        // Variant with lesser bright center
        return cache.getOrPut(factorInt) {
            val centerColor = gradient.at(1.0f)
            RadialColorGradient(
                center,
                radius,
                centerColor.withAlpha(alpha1),
                backgroundWithAlphaColor
            )
        }
    }
}