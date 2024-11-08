package ch.scorpion.jabbah.graph.view.net.node

import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewStyling

interface NodeViewStyling {

	val boundingBox: RectangularShape

	/** Returns `true` is this [EdgeViewStyling] draws large or wide areas, which is used for determining background colors. */
	val isArea: Boolean

	fun updateBoundingBox()

	/**
	 * Draws this [NodeViewStyling].
	 * This method expects that the caller passes the [CompositeColor] to be used for drawing in the provided
	 * [DrawContext], i.e. this method uses the color in [DrawContext.color] for drawing its foreground
	 * and its background.
	 */
	fun draw(context: DrawContext)
}