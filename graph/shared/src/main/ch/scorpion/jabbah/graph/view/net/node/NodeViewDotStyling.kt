package ch.scorpion.jabbah.graph.view.net.node

import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.draw.graphics.Stroke

/**
 * A [NodeViewStyling] that draws a [NodeView] as a simple dot.
 */
class NodeViewDotStyling(private val nodeView: NodeView<*>) : NodeViewStyling {

	private companion object {
		private const val STROKE_WIDTH = 0.25f
		private val SELECTION_STROKE = Stroke(width = STROKE_WIDTH)
		private const val HALF_SIZE = 4
	}

	/** ---- [NodeViewStyling] interface */

	override val boundingBox: Rectangle2D = Rectangle2D()

	override val isArea: Boolean get() = false

	override fun updateBoundingBox() {
		boundingBox.setFrame(
			nodeView.location.x - HALF_SIZE - STROKE_WIDTH, nodeView.location.y - HALF_SIZE - STROKE_WIDTH,
			(2 * (HALF_SIZE + STROKE_WIDTH)).toDouble(), (2 * (HALF_SIZE + STROKE_WIDTH)).toDouble()
		)
	}

	override fun draw(context: DrawContext) {
		context.g.color = context.color!!.foregroundColor
		context.g.fillOval(
			(nodeView.location.x - HALF_SIZE).toInt(), (nodeView.location.y - HALF_SIZE).toInt(),
			2 * HALF_SIZE, 2 * HALF_SIZE)

		// Completely cover [NodeView]s that lie beneath this one
		if (context.useContextColors) {
			context.g.stroke = SELECTION_STROKE
			context.g.drawOval(
				(nodeView.location.x - HALF_SIZE).toInt(), (nodeView.location.y - HALF_SIZE).toInt(),
				2 * HALF_SIZE, 2 * HALF_SIZE)
		}
	}
}