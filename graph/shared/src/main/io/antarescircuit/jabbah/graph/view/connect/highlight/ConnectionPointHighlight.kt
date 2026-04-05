package io.antarescircuit.jabbah.graph.view.connect.highlight

import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.draw.Drawable
import io.antarescircuit.jabbah.draw.DrawProperties
import io.antarescircuit.jabbah.draw.drawable.Unzoomable
import io.antarescircuit.jabbah.draw.graphics.Color
import io.antarescircuit.jabbah.graph.view.EdgeView

/**
 * A [Drawable] used to highlight the point where a new connecting [EdgeView] starts or ends
 * when the mouse is being moved or dragged.
 */
interface ConnectionPointHighlight : Unzoomable {

	companion object {

		/** The name of the [Color] property in [DrawProperties]. */
		const val PROP_COLOR = "graph.view.isPort.highlight.color"

		const val SIZE_HALF = 10.0
	}

	/**
	 * The location of the connection point (in model coordinates) to be highlighted.
	 * This typically corresponds with the center of the figure to be displayed, but is finally
	 * up to the implementation to interpret.
	 */
	var location: Point2D

	/**
	 * If set, this [ConnectionPointHighlight] is drawn in its alternative view, e.g. as rectangle
	 * instead of a circle.
	 */
	var alternativeView: Boolean
}