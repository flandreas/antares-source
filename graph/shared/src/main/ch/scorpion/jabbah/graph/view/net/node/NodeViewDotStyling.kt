package ch.scorpion.jabbah.graph.view.net.node

import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.base.geom.Rectangle2D

/**
 * A [NodeViewStyling] that draws a [NodeView] as a simple dot.
 */
class NodeViewDotStyling(private val nodeView: NodeView<*>) : NodeViewStyling {

	private companion object {
		private const val HALF_SIZE = 4
	}

	/** ---- [NodeViewStyling] interface */

	override val boundingBox: Rectangle2D = Rectangle2D()

	override val isArea: Boolean get() = false

	override fun updateBoundingBox() {
		boundingBox.setFrame(
			nodeView.location.x - HALF_SIZE, nodeView.location.y - HALF_SIZE,
			(2 * HALF_SIZE).toDouble(), (2 * HALF_SIZE).toDouble()
		)
	}

	override fun draw(context: DrawContext) {
		context.g.color = context.color!!.foregroundColor
		context.g.fillOval(
			(nodeView.location.x - HALF_SIZE).toInt(), (nodeView.location.y - HALF_SIZE).toInt(),
			2 * HALF_SIZE, 2 * HALF_SIZE)

		// Completely cover [NodeView]s that lie beneath this one
		if (context.useContextColors) {
			context.g.drawOval(
				(nodeView.location.x - HALF_SIZE).toInt(), (nodeView.location.y - HALF_SIZE).toInt(),
				2 * HALF_SIZE, 2 * HALF_SIZE)
		}
	}
}