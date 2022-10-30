package ch.scorpion.jabbah.draw

import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.Graphics2D
import ch.scorpion.jabbah.draw.style.Stylable

/**
 * A context used in all drawing activities.
 *
 * @param modelClip the optional clipping area in model space. [Drawable]s can avoid
 * repaint themselves if their bounding box doesn't intersect this region. Usually `null`
 * if the entire [View] area is to be drawn. Is writable in order to support clipping at local
 * coordinate systems of [DrawableContainer]s along chains of drawing calls.
 */
class DrawContext(
	val g: Graphics2D,
	var modelClip: Rectangle2D? = null,
	val appContext: Any? = null
) {

	/** Instructs a [Drawable] to use the colors of this [DrawContext] instead of its own colors.*/
	var useContextColors: Boolean = false

	/** The [Color] used for drawing if [useContextColors] is `true`.*/
	var color: CompositeColor? = null

	/**
	 * The optional [Stylable] to be used as a source for styling information like [Color].
	 * Can be used by composite object to let outer components control how inner components are drawn.
	 */
	var stylable: Stylable? = null

	var selectionColor: CompositeColor? = null

	/**
	 * Convenience method for choosing to use the specified [CompositeColor] or this [DrawContext]'s color
	 * depending on the current value of [useContextColors].
	 */
	fun choose(color: CompositeColor): CompositeColor =
		if (useContextColors) this.color!! else color

	fun chooseForeground(color: Color): Color =
		if (useContextColors) this.color!!.foregroundColor else color

	fun chooseBackground(color: Color): Color =
		if (useContextColors) this.color!!.backgroundColor else color

	fun <T> castedAppContext(): T? {
		@Suppress("UNCHECKED_CAST")
		return appContext as T?
	}

	/**
	 * Returns the [CompositeColor] of the [Stylable] of this [DrawContext], or the specified [CompositeColor]
	 * if no [Stylable] is set.
	 */
	fun styleColor(defaultColor: CompositeColor): CompositeColor = stylable?.color ?: defaultColor
}