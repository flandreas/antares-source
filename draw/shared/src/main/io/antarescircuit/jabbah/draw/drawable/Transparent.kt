package io.antarescircuit.jabbah.draw.drawable

import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.base.geom.RectangularShape
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.Drawable
import io.antarescircuit.jabbah.draw.graphics.Color
import kotlin.math.floor

/**
 * A [Drawable] whose transparency can be controlled.
 *
 * Transparent [Drawable]s must add the transparency value to all [Color]s they use when drawing.
 * The [Transparent] interface can be used by objects that produce certain visual effects on a [Drawable],
 * such as glowing.
 */
interface Transparent : Drawable {

    companion object {
        const val FULLY_TRANSPARENT = 0
        const val FULLY_OPAQUE = 255

	    fun applyTo(transparency: Int, color: Color): Color {
		    if (transparency == FULLY_OPAQUE) {
			    return color
		    }
		    val rate = transparency.toFloat() / FULLY_OPAQUE
		    return color.withAlpha(floor(color.alpha * rate).toInt())
	    }
    }

    /**
     * Represents the transparency value as an alpha color channel value between 0 and 255 (inclusive),
     * where 0 means complete transparency, and 255 means complete opacity.
     */
    var transparency: Int

	/** Applies the transparency to the specified [Color] and returns the new [Color].*/
	fun applyTo(color: Color): Color  = applyTo(transparency, color)

}

/**
 * An implementation of [Transparent] that can be used as a mixin in an owning [Drawable].
 */
class TransparentImpl(val owner: Drawable) : Transparent, Drawable by owner {

    override var transparency: Int = Transparent.FULLY_OPAQUE
        set(value) {
            if (value > Transparent.FULLY_OPAQUE || value < Transparent.FULLY_TRANSPARENT) {
                throw IllegalArgumentException("transparency '$value' must be between 0 and 255")
            }
            field = value
            owner.invalidate()
        }
}

/**
 * Bridges the [Transparent] and [Drawable] interfaces to classes that don't implement those.
 * Used e.g. to implement glowing effects on native UI classes like icons or buttons.
 *
 * @property target called with the current [transparency] whenever it is updated
 */
class TransparentBridge(
	private val target: (Int) -> Unit
) : AbstractDrawable(), Transparent {

	override var transparency: Int = 0
		set(value) {
			field = value
			target(field)
		}

	override val boundingBox: RectangularShape = Rectangle2D()

	override fun draw(context: DrawContext) { }

	override fun contains(x: Double, y: Double): Boolean = false
}