package ch.scorpion.jabbah.graph.view.net.edge

import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.RectangularShape
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

	private val _boundingBox = Rectangle2D()
	override val boundingBox: RectangularShape get() = _boundingBox

	override val isArea: Boolean get() = false

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

		edgeView.polyline.beginLineTerminator?.draw(context)
		edgeView.polyline.endLineTerminator?.draw(context)
	}

	override fun updateBoundingBox() {
		if (edgeView.polyline.pointsCount > 0) {
			_boundingBox.setFrame(edgeView.polyline.getPointAt(0).x, edgeView.polyline.getPointAt(0).y, 0.0, 0.0)
		}
		_boundingBox.add(edgeView.polyline.boundingBox)
		if (edgeView.origin == null) {
			_boundingBox.add(edgeView.originEndpointView.boundingBox)
		} else if (edgeView.origin?.connectableView is NodeView<*>) {
			_boundingBox.add(edgeView.origin!!.connectableView.boundingBox)
		}
		if (edgeView.destination == null) {
			_boundingBox.add(edgeView.destinationEndpointView.boundingBox)
		} else if (edgeView.destination?.connectableView is NodeView<*>) {
			_boundingBox.add(edgeView.destination!!.connectableView.boundingBox)
		}
		_boundingBox.setFrame(
			_boundingBox.x - edgeView.stroke.width, _boundingBox.y - edgeView.stroke.width,
			_boundingBox.width + 2 * edgeView.stroke.width, _boundingBox.height + 2 * edgeView.stroke.width)
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