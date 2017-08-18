package ch.scorpion.jabbah.draw

import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.Graphics2D

/**
 * A context used in all drawing activities.
 */
open class DrawContext(val g: Graphics2D, val appContext: Any? = null) {

    /** Instructs a [Drawable] to use the colors of this [DrawContext] instead of its own colors.*/
    var useContextColors: Boolean = false

    /** The [Color] used for drawing if [useContextColors] is `true`.*/
    var color: CompositeColor? = null

    var selectionColor: CompositeColor? = null

    /**
     * Convenience method for choosing to use the specified [CompositeColor] or this [DrawContext]'s color
     * depending on the current value of [useContextColors].
     */
    fun choose(color: CompositeColor): CompositeColor {
        return if (useContextColors) this.color!! else color
    }

    fun <T> castedAppContext(): T? {
        return appContext as T?
    }
}