package ch.scorpion.jabbah.graph.view.connect.highlight

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.draw.graphics.Cursor
import ch.scorpion.jabbah.draw.module.DrawModule
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.graph.view.port.PortView

object ConnectionPointHighlighter {

	private val LOG by logger(ConnectionPointHighlighter::class)

	/** The highlight of the currently snapped origin or destination [ch.scorpion.jabbah.graph.view.port.PortView], else `null`. */
	var portViewHighlight: ConnectionPointHighlight? = null
		private set

	val hasPortViewHighlight: Boolean get() = portViewHighlight != null

	/** The [ch.scorpion.jabbah.edit.DrawingView] to which [portViewHighlight] has been added (if any). */
	private var view: DrawingView<*>? = null

	fun displayPortViewHighlight(
        view: DrawingView<*>,
        location: Point2D,
        alternativeView: Boolean = false,
        highlight: ConnectionPointHighlight = DrawModule.properties.get(PortView.Companion.PROP_HIGHLIGHT)
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