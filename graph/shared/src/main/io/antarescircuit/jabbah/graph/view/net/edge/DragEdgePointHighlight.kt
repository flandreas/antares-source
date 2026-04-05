package io.antarescircuit.jabbah.graph.view.net.edge

import io.antarescircuit.jabbah.base.Properties
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.base.geom.RectangularShape
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.Drawable
import io.antarescircuit.jabbah.draw.ZoomPan
import io.antarescircuit.jabbah.draw.drawable.AbstractDrawable
import io.antarescircuit.jabbah.draw.drawable.Unzoomable
import io.antarescircuit.jabbah.draw.graphics.Color
import io.antarescircuit.jabbah.draw.module.DrawModule
import io.antarescircuit.jabbah.edit.select.Handle
import io.antarescircuit.jabbah.graph.view.EdgeView

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

		private val STROKE = DrawModule.properties.getStroke(Handle.PROP_STROKE)
	}

	/** The index of the [Point2D] to be manipulated, if any. */
	var pointIndex: Int? = null

	/** ---- [Unzoomable] interface */

	override var zoomPan: ZoomPan? = null
		set(value) {
			if (value != zoomPan) {
				invalidate()
				field = value
				invalidate()
				update()
			}
		}

	/** ---- [Drawable] interface */

	override val boundingBox: RectangularShape
		get() {
			val zf = zoomPan?.zoomFactor ?: 1.0
			val size = DrawModule.properties.getInt(PROP_HALF_SIZE) / zf
			val bbox = edgeView.boundingBox
			return Rectangle2D(bbox).expandBy(size + STROKE.width) as Rectangle2D
		}

	override fun draw(context: DrawContext) {
		val oldColor = context.g.color
		context.g.color = DrawModule.properties.getColor(PROP_COLOR)
		context.g.stroke = STROKE
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