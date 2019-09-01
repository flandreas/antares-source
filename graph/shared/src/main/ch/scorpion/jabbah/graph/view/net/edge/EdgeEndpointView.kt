package ch.scorpion.jabbah.graph.view.net.edge

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.InputEventContext
import ch.scorpion.jabbah.draw.InputEventHandler
import ch.scorpion.jabbah.draw.drawable.AbstractStyledDrawable
import ch.scorpion.jabbah.draw.drawable.Locatable
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.connect.AbstractDragEdgeViewEndpointConnector
import ch.scorpion.jabbah.graph.view.style.GraphStyleType

/**
 * A [Drawable] being drawn at the unconnected end points of an [EdgeView].
 * Draws a circle at the end point location.
 */
class EdgeEndpointView(
	private val edgeView: EdgeView<*>,
	private val connectorSupplier: () -> AbstractDragEdgeViewEndpointConnector,
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
		connectorSupplier.invoke().useFor(edgeView)
		return connectorSupplier.invoke().handler as InputEventHandler<T>
	}

	override fun draw(context: DrawContext) {
		val oldColor = context.g.color
		context.g.color = styleProvider.getStyle(StyleType.BACKGROUND).color.backgroundColor
		context.g.fillOval(bounds.x.toInt(), bounds.y.toInt(), bounds.width.toInt(), bounds.height.toInt())
		context.g.color = oldColor
		context.g.drawOval(bounds.x.toInt(), bounds.y.toInt(), bounds.width.toInt(), bounds.height.toInt())
		context.g.color = oldColor
	}

	override val boundingBox: Rectangle2D get() = bounds

	override fun contains(x: Double, y: Double): Boolean = bounds.contains(x, y)

	/** ---- [EdgeEndpointView] */

	private fun updateGeometry() {
		bounds.setFrame(location.x - SIZE_HALF - 1, location.y - SIZE_HALF - 1, 2.0 * (SIZE_HALF + 1), 2.0 * (SIZE_HALF + 1))
	}
}