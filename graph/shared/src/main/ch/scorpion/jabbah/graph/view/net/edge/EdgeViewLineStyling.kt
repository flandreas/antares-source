package ch.scorpion.jabbah.graph.view.net.edge

import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.module.DrawModule
import ch.scorpion.jabbah.graph.view.net.node.NodeView

/**
 * An [EdgeViewStyling] that draws an [EdgeView] as thin lines.
 *
 * [EdgeViewLineStyling] also draws [NodeView] to which it is connected, because otherwise they would not be
 * visible when using highlighting.
 */
class EdgeViewLineStyling(private val edgeView: EdgeView<*>) : EdgeViewStyling {

	override val width: Int get() = edgeView.style.stroke.width.toInt()

	override val boundingBox: Rectangle2D = Rectangle2D()

	override fun draw(context: DrawContext) {
		context.g.color = context.color!!.foregroundColor
		context.g.draw(edgeView.polyline)

		if (DrawModule.debugGfx) {
			drawDebug(context)
		}

		if (edgeView.origin == null) {
			edgeView.originEndpointView.draw(context)
		} else if (edgeView.origin?.connectableView is NodeView<*>) {
			edgeView.origin!!.connectableView.draw(context)
		}
		if (edgeView.destination == null) {
			edgeView.destinationEndpointView.draw(context)
		} else if (edgeView.destination?.connectableView is NodeView<*>) {
			edgeView.destination!!.connectableView.draw(context)
		}

		if (edgeView.polyline.endLineTerminator != null) {
			edgeView.polyline.endLineTerminator!!.draw(context)
		}
	}

	override fun updateBoundingBox() {
		if (edgeView.polyline.pointsCount > 0) {
			boundingBox.setFrame(edgeView.polyline.getPointAt(0).x, edgeView.polyline.getPointAt(0).y, 0.0, 0.0)
		}
		boundingBox.add(edgeView.polyline.boundingBox)
		if (edgeView.origin == null) {
			boundingBox.add(edgeView.originEndpointView.boundingBox)
		} else if (edgeView.origin?.connectableView is NodeView<*>) {
			boundingBox.add(edgeView.origin!!.connectableView.boundingBox)
		}
		if (edgeView.destination == null) {
			boundingBox.add(edgeView.destinationEndpointView.boundingBox)
		} else if (edgeView.destination?.connectableView is NodeView<*>) {
			boundingBox.add(edgeView.destination!!.connectableView.boundingBox)
		}
		boundingBox.setFrame(
			boundingBox.x - edgeView.stroke.width, boundingBox.y - edgeView.stroke.width,
			boundingBox.width + 2 * edgeView.stroke.width, boundingBox.height + 2 * edgeView.stroke.width)
	}

	/** ---- [EdgeViewLineStyling]  */

	private fun drawDebug(context: DrawContext) {
		val oldColor = context.g.color
		val oldStroke = context.g.stroke
		context.g.color = Color.GREEN
		context.g.stroke = DrawModule.DEBUG_STROKE
		for (i in 0 until edgeView.polyline.pointsCount - 1) {
			val begin = edgeView.polyline.getPointAt(i)
			val end = edgeView.polyline.getPointAt(i + 1)

			context.g.drawOval(begin.x.toInt() - 2, begin.y.toInt() - 2, 4, 4)
			context.g.drawOval(end.x.toInt() - 2, end.y.toInt() - 2, 4, 4)
		}

		context.g.color = Color.GRAY
		context.g.draw(boundingBox)

		context.g.color = oldColor
		context.g.stroke = oldStroke
	}
}