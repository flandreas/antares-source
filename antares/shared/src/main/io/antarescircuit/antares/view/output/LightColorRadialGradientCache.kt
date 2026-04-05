package io.antarescircuit.antares.view.output

import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.draw.graphics.RadialColorGradientCache
import io.antarescircuit.jabbah.draw.graphics.RadialColorGradient
import io.antarescircuit.jabbah.draw.graphics.Color
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleType

/**
 * A cache that yields a [RadialColorGradientCache] for a [LightColor].
 *
 * Can be used for getting a [RadialColorGradient] for drawing a radial halo around and LED
 * during simulation. The properties [center] and [radius] are determined by the components that wants
 * to render a halo, so every type of component will maintain its own instance of [LightColorRadialGradientCache].
 */
class LightColorRadialGradientCache(
    private val center: Point2D,
    private val radius: Int,
    private val backgroundColor: Color = DrawStyleModule.styleProvider.getStyle(StyleType.BACKGROUND).color.backgroundColor
) {
    private val gradientCaches = mutableMapOf<LightColor, RadialColorGradientCache>()

    /**
     * Returns a [RadialColorGradientCache] that can then be accessed for various center [Color],
     * e.g. when the center color of an LED varies due to changing current.
     *
     * Note that the returned [RadialColorGradientCache] has a perimeter color with alpha value 0
     */
    fun forLightColor(lightColor: LightColor): RadialColorGradientCache =
        gradientCaches.getOrPut(lightColor) {
            RadialColorGradientCache(
                center,
                radius,
                backgroundColor
            )
        }
}