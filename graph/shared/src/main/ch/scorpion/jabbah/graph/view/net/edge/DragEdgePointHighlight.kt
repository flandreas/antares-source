package ch.scorpion.jabbah.graph.view.net.edge

import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.ZoomPan
import ch.scorpion.jabbah.draw.drawable.AbstractDrawable
import ch.scorpion.jabbah.draw.drawable.Unzoomable
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.module.DrawModule
import ch.scorpion.jabbah.graph.view.EdgeView

/**
 * A [Drawable] that draws a small square at every draggable point of an [EdgeView] in order to indicate that
 * the point can be dragged by the user.
 */
class DragEdgePointHighlight(private val edgeView: EdgeView<*>) : AbstractDrawable(), Unzoomable {

	companion object {

		/** The name of [Color] property in [Properties] that determines the color of the drawn square.*/
		const val PROP_COLOR = "graph.view.DragEdgePointHighlight.color"

		/** The name of the [Int] property in [Properties] that determines the size of the drawn square.*/
		const val PROP_HALF_SIZE = "graph.view.DragEdgePointHighlight.size"
	}

	/** The index of the [Point2D] to be manipulated, if any. */
	var pointIndex: Int? = null

	/** ---- [Unzoomable] interface */

	override var zoomPan: ZoomPan? = null

	/** ---- [Drawable] interface */

	override val boundingBox: Rectangle2D
		get() {
			val size = DrawModule.properties.getInt(PROP_HALF_SIZE)
			val bbox = edgeView.boundingBox
			return Rectangle2D(bbox.x - size - 1, bbox.y - size - 1, bbox.width + 2 * size + 1, bbox.height + 2 * size + 1)
		}

	override fun draw(context: DrawContext) {
		val oldColor = context.g.color
		context.g.color = DrawModule.properties.getColor(PROP_COLOR)
		val size = DrawModule.properties.getInt(PROP_HALF_SIZE)

		for (i in 1..edgeView.segmentPointCount - 2) {
			val p = zoomPan!!.transform.modelToView(edgeView.getSegmentPoint(i))
			context.g.drawRect(p.x.toInt() - size, p.y.toInt() - size, 2 * size, 2 * size)
		}
		context.g.color = oldColor
	}

	override fun contains(x: Double, y: Double): Boolean {
		return edgeView.contains(x, y)
	}

	/** ---- [DragEdgePointHighlight] */

	fun updateMouseLocation(x: Double, y: Double) {
		val size = DrawModule.properties.getInt(PROP_HALF_SIZE)
		var index = edgeView.polyline.findPoint(x, y, size)
		if (index == 0 || index == edgeView.segmentPointCount - 1) {
			index = null
		}
		if (pointIndex != index) {
			this.pointIndex = index
			invalidate()
			validate()
		}
	}
}