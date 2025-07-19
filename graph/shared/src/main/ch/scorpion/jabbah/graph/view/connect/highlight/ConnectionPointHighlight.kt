package ch.scorpion.jabbah.graph.view.connect.highlight

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.drawable.Unzoomable

/**
 * A [ch.scorpion.jabbah.draw.Drawable] used to highlight the point where a new connecting [ch.scorpion.jabbah.graph.view.EdgeView] starts or ends when the mouse
 * is being moved or dragged.
 */
interface ConnectionPointHighlight : Unzoomable {

	companion object {
		/** The name of the [ch.scorpion.jabbah.draw.graphics.Color] property in [ch.scorpion.jabbah.draw.DrawProperties] */
		const val PROP_COLOR = "graph.view.isPort.highlight.color"
		const val SIZE_HALF = 10.0
	}

	var location: Point2D
	var alternativeView: Boolean
}