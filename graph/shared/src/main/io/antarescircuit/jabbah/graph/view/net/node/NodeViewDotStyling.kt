package io.antarescircuit.jabbah.graph.view.net.node

import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.base.geom.RectangularShape
import io.antarescircuit.jabbah.draw.graphics.Stroke

/**
 * A [NodeViewStyling] that draws a [NodeView] as a simple dot.
 */
class NodeViewDotStyling(private val nodeView: NodeView<*>) : NodeViewStyling {

	private companion object {
		private const val STROKE_WIDTH = 0.25f
		private val SELECTION_STROKE = Stroke(width = STROKE_WIDTH)
		private const val HALF_SIZE = 3.5
	}

	/** ---- [NodeViewStyling] interface */

	private val _boundingBox = Rectangle2D()
	override val boundingBox: RectangularShape get() = _boundingBox

	override val isArea: Boolean get() = false

	override fun updateBoundingBox() {
		_boundingBox.setFrame(
			nodeView.location.x - HALF_SIZE - STROKE_WIDTH, nodeView.location.y - HALF_SIZE - STROKE_WIDTH,
            (2 * (HALF_SIZE + STROKE_WIDTH)), (2 * (HALF_SIZE + STROKE_WIDTH))
		)
	}

	override fun draw(context: DrawContext) {
		context.g.color = context.color!!.foregroundColor
		context.g.fillOval(
			nodeView.location.x - HALF_SIZE, nodeView.location.y - HALF_SIZE,
			2 * HALF_SIZE, 2 * HALF_SIZE)

		// Completely cover [NodeView]s that lie beneath this one
		if (context.useContextColors) {
			context.g.stroke = SELECTION_STROKE
			context.g.drawOval(
				nodeView.location.x - HALF_SIZE, nodeView.location.y - HALF_SIZE,
				2 * HALF_SIZE, 2 * HALF_SIZE)
		}
	}
}