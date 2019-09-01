package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.draw.drawable.Locatable
import ch.scorpion.jabbah.draw.drawable.Unzoomable
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.DrawProperties
import ch.scorpion.jabbah.draw.drawable.AbstractRectangularUnzoomable
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.Cursor
import ch.scorpion.jabbah.draw.graphics.Stroke
import ch.scorpion.jabbah.draw.module.DrawModule
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.graph.view.port.PortView

/**
 * A [Drawable] used to highlight the point where a new connecting [EdgeView] starts or ends when the mouse
 * is being moved or dragged.
 */
interface ConnectionPointHighlight : Unzoomable {
	var location: Point2D
	var alternativeView: Boolean
}

object ConnectionPointHighlighter {

	private val LOG by logger(ConnectionPointHighlighter::class)

	/** The highlight of the currently snapped origin or destination [PortView], else `null`. */
	var portViewHighlight: ConnectionPointHighlight? = null
		private set

	val hasPortViewHighlight: Boolean get() = portViewHighlight != null

	fun displayPortViewHighlight(view: DrawingView<*>, location: Point2D, alternativeView: Boolean = false) {
		LOG.trace("displayPortViewHighlight at $location")
		if (portViewHighlight == null) {
			view.setCursor(Cursor.CROSSHAIR)
			portViewHighlight = DrawModule.properties.get<ConnectionPointHighlight>(PortView.PROP_HIGHLIGHT)
			portViewHighlight!!.location = location
			portViewHighlight!!.alternativeView = alternativeView
			view.ghostContainer.add(portViewHighlight!!)
		} else {
			portViewHighlight!!.location = location
		}
		portViewHighlight!!.validate()
	}

	/** Removes the previously displayed [ConnectionPointHighlight] from the [DrawingView]. */
	fun removePortViewHighlight(view: DrawingView<*>) {
		if (portViewHighlight != null) {
			LOG.trace("removePortViewHighlight")
			view.ghostContainer.remove(portViewHighlight!!)
			view.ghostContainer.validate()
			portViewHighlight = null
		}
	}
}

/**
 * Draws a [ConnectionPointHighlight] as a circle.
 */
class ConnectionPointHighlightCircle : AbstractRectangularUnzoomable(SIZE_HALF), ConnectionPointHighlight {

	companion object {
		/** The name of the [Color] property in [DrawProperties] */
		const val PROP_COLOR = "graph.view.isPort.highlight.color"
		const val SIZE_HALF = 6.0
		val STROKE = Stroke()
	}

	override val lineWidth: Double get() = STROKE.width.toDouble()

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
		val rect = getViewRectangle()
		context.g.drawOval(rect.x.toInt(), rect.y.toInt(), rect.width.toInt(), rect.height.toInt())
		context.g.fillOval(rect.x.toInt(), rect.y.toInt(), rect.width.toInt(), rect.height.toInt())
	}

	private fun drawAlternativeView(context: DrawContext) {
		val rect = getViewRectangle()
		context.g.drawRect(rect.x.toInt(), rect.y.toInt(), rect.width.toInt(), rect.height.toInt())
		context.g.fillRect(rect.x.toInt(), rect.y.toInt(), rect.width.toInt(), rect.height.toInt())
	}
}