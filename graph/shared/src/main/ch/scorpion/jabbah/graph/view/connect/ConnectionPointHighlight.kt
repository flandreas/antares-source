package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.DrawProperties
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.drawable.AbstractRectangularUnzoomable
import ch.scorpion.jabbah.draw.drawable.Unzoomable
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.Cursor
import ch.scorpion.jabbah.draw.graphics.Stroke
import ch.scorpion.jabbah.draw.module.DrawModule
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.connect.ConnectionPointHighlight.Companion.PROP_COLOR
import ch.scorpion.jabbah.graph.view.connect.ConnectionPointHighlight.Companion.SIZE_HALF
import ch.scorpion.jabbah.graph.view.port.PortView
import ch.scorpion.jabbah.graph.view.style.GraphStyleType

/**
 * A [Drawable] used to highlight the point where a new connecting [EdgeView] starts or ends when the mouse
 * is being moved or dragged.
 */
interface ConnectionPointHighlight : Unzoomable {

	companion object {
		/** The name of the [Color] property in [DrawProperties] */
		const val PROP_COLOR = "graph.view.isPort.highlight.color"
		const val SIZE_HALF = 10.0
	}

	var location: Point2D
	var alternativeView: Boolean
}

object ConnectionPointHighlighter {

	private val LOG by logger(ConnectionPointHighlighter::class)

	/** The highlight of the currently snapped origin or destination [PortView], else `null`. */
	var portViewHighlight: ConnectionPointHighlight? = null
		private set

	val hasPortViewHighlight: Boolean get() = portViewHighlight != null

	/** The [DrawingView] to which [portViewHighlight] has been added (if any). */
	private var view: DrawingView<*>? = null

	fun displayPortViewHighlight(
		view: DrawingView<*>,
		location: Point2D,
		alternativeView: Boolean = false,
		highlight: ConnectionPointHighlight = DrawModule.properties.get(PortView.PROP_HIGHLIGHT)
	) {
		LOG.trace("displayPortViewHighlight at $location")
		if (portViewHighlight == null) {
			view.setCursor(Cursor.CROSSHAIR)
			portViewHighlight = highlight
			portViewHighlight!!.location = location
			portViewHighlight!!.alternativeView = alternativeView
			view.ghostContainer.add(portViewHighlight!!)
			this.view = view
		} else {
			portViewHighlight!!.location = location
		}
		portViewHighlight!!.validate()
	}

	/** Removes the previously displayed [ConnectionPointHighlight] from the [DrawingView]. */
	fun removePortViewHighlight() {
		if (portViewHighlight != null) {
			view?.let {
				LOG.trace("removePortViewHighlight")
				it.ghostContainer.remove(portViewHighlight!!)
				it.ghostContainer.validate()
				portViewHighlight = null
				view = null
			}
		}
	}
}

/**
 * Draws a [ConnectionPointHighlight] as a circle.
 */
class ConnectionPointHighlightCircle : AbstractRectangularUnzoomable(SIZE_HALF), ConnectionPointHighlight {

	companion object {

		private const val INSET = 3

		val stroke = Stroke(DrawStyleModule.styleProvider.getStyle(GraphStyleType.EDGE).stroke.width)

		fun drawNormalViewAt(location: Point2D, context: DrawContext) {
			context.g.color = DrawModule.properties.getColor(PROP_COLOR)
			context.g.fillCircle(location.x, location.y, SIZE_HALF - INSET)
			context.g.stroke = stroke
			context.g.drawCircle(location.x, location.y, SIZE_HALF)
		}
	}

	override val lineWidth: Double get() = stroke.width.toDouble()

	override var alternativeView: Boolean = false
		set(value) {
			if (value != alternativeView) {
				invalidate()
				field = value
				invalidate()
				validate()
			}
		}

	override fun draw(context: DrawContext) {
		context.g.color = DrawModule.properties.getColor(PROP_COLOR)
		if (alternativeView) {
			drawAlternativeView(context)
		} else {
			drawNormalView(context)
		}
	}

	private fun drawNormalView(context: DrawContext) {
		drawNormalViewAt(getViewRectangle().center, context)
	}

	private fun drawAlternativeView(context: DrawContext) {
		val rect = getViewRectangle()
		context.g.fillRect(rect.x.toInt() + INSET, rect.y.toInt() + INSET, rect.width.toInt() - 2*INSET, rect.height.toInt() - 2*INSET)
		context.g.stroke = stroke
		context.g.drawRect(rect.x.toInt(), rect.y.toInt(), rect.width.toInt(), rect.height.toInt())
	}
}

class ConnectionPointReconnect : AbstractRectangularUnzoomable(SIZE_HALF), ConnectionPointHighlight {

	companion object {
		private const val F = 3.0
		private const val SIZE_HALF = F * 6.0
		private const val INSET = F * 3.0
		private const val ARROW = F * 2.0
		private val stroke = Stroke(DrawStyleModule.styleProvider.getStyle(GraphStyleType.EDGE).stroke.width)

		private val NORTH = System.createPath()
			.moveTo(0.0, -SIZE_HALF)
			.lineTo(ARROW, -SIZE_HALF + ARROW)
			.lineTo(-ARROW, -SIZE_HALF + ARROW)
			.close()

		private val SOUTH = System.createPath()
			.moveTo(0.0, SIZE_HALF)
			.lineTo(ARROW, SIZE_HALF - ARROW)
			.lineTo(-ARROW, SIZE_HALF - ARROW)
			.close()

		private val EAST = System.createPath()
			.moveTo(SIZE_HALF, 0.0)
			.lineTo(SIZE_HALF - ARROW, -ARROW)
			.lineTo(SIZE_HALF - ARROW, ARROW)
			.close()

		private val WEST = System.createPath()
			.moveTo(-SIZE_HALF, 0.0)
			.lineTo(-SIZE_HALF + ARROW, -ARROW)
			.lineTo(-SIZE_HALF + ARROW, ARROW)
			.close()
	}

	override val lineWidth: Double get() = stroke.width.toDouble()
	override var alternativeView: Boolean = false

	override fun draw(context: DrawContext) {
		context.g.color = DrawModule.properties.getColor(PROP_COLOR)
		context.g.stroke = stroke

		val rect = getViewRectangle()
		context.g.fillOval(
			rect.x.toInt() + INSET, rect.y.toInt() + INSET,
			rect.width.toInt() - 2 * INSET, rect.height.toInt() - 2* INSET)

		context.translated(rect.center) {
			it.g.fill(NORTH)
			it.g.fill(SOUTH)
			it.g.fill(EAST)
			it.g.fill(WEST)
		}
	}
}