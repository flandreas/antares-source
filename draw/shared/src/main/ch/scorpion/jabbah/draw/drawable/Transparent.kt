package ch.scorpion.jabbah.draw.drawable

import ch.scorpion.jabbah.base.exception.IllegalArgumentException
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.graphics.Color

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
		    if (color.alpha == transparency) {
			    return color
		    }
		    return Color(color, transparency)
	    }
    }

    /**
     * Represents the transparency value as an alpha color channel value between 0 and 255 (inclusive),
     * where 0 means complete transparency, and 255 means complete opacity.
     */
    var transparency: Int

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

	/** Applies the transparency to the specified [Color] and returns the new [Color].*/
	fun applyTo(color: Color): Color {
		return Transparent.applyTo(transparency, color)
	}
}