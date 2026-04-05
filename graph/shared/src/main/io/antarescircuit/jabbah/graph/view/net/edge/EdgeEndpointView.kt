package io.antarescircuit.jabbah.graph.view.net.edge

import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.base.geom.RectangularShape
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.Drawable
import io.antarescircuit.jabbah.draw.InputEventContext
import io.antarescircuit.jabbah.draw.InputEventHandler
import io.antarescircuit.jabbah.draw.drawable.AbstractStyledDrawable
import io.antarescircuit.jabbah.draw.drawable.Locatable
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.draw.style.StyleType
import io.antarescircuit.jabbah.edit.EditInputEventContext
import io.antarescircuit.jabbah.graph.view.EdgeView
import io.antarescircuit.jabbah.graph.view.style.GraphStyleType

/**
 * A [Drawable] being drawn at the unconnected end points of an [EdgeView].
 * Draws a circle at the end point location.
 */
class EdgeEndpointView(
	val edgeView: EdgeView<*>,
	private val type: EdgeViewEndpointType,
	styleProvider: StyleProvider
) : AbstractStyledDrawable(GraphStyleType.EDGE, styleProvider), Locatable {

	companion object {
		const val SIZE_HALF = 6
	}

	/** Holds the [Rectangle2D] in absolute model coordinate space that makes up this [EdgeEndpointView]. */
	private val bounds = Rectangle2D()

	/** ---- [Locatable] */

	/** The center of [bounds] in absolute model coordinate space. */
	override var location: Point2D = Point2D.ZERO
		set(value) {
			if (field != value) {
				invalidate()
				field = Point2D(value.x, value.y)
				updateGeometry()
				invalidate()
				update()
			}
		}

	override fun moveBy(dx: Double, dy: Double) {
		location = Point2D(location.x + dx, location.y + dy)
	}

	/** ---- [Drawable] interface */

	override fun <T : InputEventContext> getInputEventHandler(context: T): InputEventHandler<T> {
		with (type.dragConnector) {
			useFor(edgeView, context as EditInputEventContext)
			return handler as InputEventHandler<T>
		}
	}

	override fun draw(context: DrawContext) {
		val oldColor = context.g.color
		val oldStroke = context.g.stroke
		context.g.color = styleProvider.getStyle(StyleType.BACKGROUND).color.backgroundColor
		context.g.fillOval(bounds.x.toInt(), bounds.y.toInt(), bounds.width.toInt(), bounds.height.toInt())
		context.g.color = oldColor
		context.g.stroke = styleProvider.getStyle(GraphStyleType.EDGE).stroke
		context.g.drawOval(bounds.x.toInt(), bounds.y.toInt(), bounds.width.toInt(), bounds.height.toInt())
		context.g.stroke = oldStroke
		context.g.color = oldColor
	}

	override val boundingBox: RectangularShape get() = bounds

	override fun contains(x: Double, y: Double): Boolean = bounds.contains(x, y)

	/** ---- [EdgeEndpointView] */

	private fun updateGeometry() {
		bounds.setFrame(location.x - SIZE_HALF, location.y - SIZE_HALF, 2.0 * SIZE_HALF, 2.0 * SIZE_HALF)
	}
}