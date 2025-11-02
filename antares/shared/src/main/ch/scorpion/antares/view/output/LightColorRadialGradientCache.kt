package ch.scorpion.antares.view.output

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.graphics.RadialColorGradientCache
import ch.scorpion.jabbah.draw.graphics.RadialColorGradient
import ch.scorpion.jabbah.draw.graphics.Color

/**
 * A cache that yields a [RadialColorGradientCache] for a [LightColor].
 *
 * Can be used for getting a [RadialColorGradient] for drawing a radial halo around and LED
 * during simulation. The properties [center] and [radius] are determined by the components that wants
 * to render a halo, so every type of component will maintain its own instance of [LightColorRadialGradientCache].
 */
class LightColorRadialGradientCache(
    val center: Point2D,
    val radius: Double,
) {
    private val gradientCaches = mutableMapOf<LightColor, RadialColorGradientCache>()

    /**
     * Returns a [RadialColorGradientCache] that can then be accessed for various center [Color],
     * e.g. when the center color of an LED various due to changing current.
     *
     * Note that the returned [RadialColorGradientCache] has a perimeter color with an alpha component 0
     */
    fun forLightColor(lightColor: LightColor): RadialColorGradientCache =
        gradientCaches.getOrPut(lightColor) {
            RadialColorGradientCache(
                center,
                radius,
                lightColor.gradient.at(0.0f).withAlpha(0)
            )
        }
}