package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.draw.InputEventHandler
import ch.scorpion.jabbah.draw.InputEventHandlerAdapter
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.EditInputEventContext
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.graph.view.port.PortView
import ch.scorpion.jabbah.draw.graphics.Cursor
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.draw.module.DrawModule

/**
 * Abstract base implementation of an [InputEventHandler] that supports highlighting connection points.
 */
abstract class AbstractConnectionPointHighlighter(
    successor: InputEventHandler<EditInputEventContext>? = null
) : InputEventHandlerAdapter<EditInputEventContext>(successor) {

    private val LOG by logger(AbstractConnectionPointHighlighter::class)

    /** The highlight of the currently snapped origin or destination [PortView], else `null`. */
    protected var portViewHighlight: ConnectionPointHighlight? = null

    protected fun displayPortViewHighlight(view: DrawingView<*>, location: Point2D) {
        LOG.trace("displayPortViewHighlight at $location")
        if (portViewHighlight == null) {
            view.setCursor(Cursor.DEFAULT)
            portViewHighlight = DrawModule.properties.get<ConnectionPointHighlight>(PortView.PROP_HIGHLIGHT)
            portViewHighlight!!.location = location
            view.ghostContainer.add(portViewHighlight!!)
        } else {
            portViewHighlight!!.location = location
        }
        portViewHighlight!!.validate()
    }

    /** Removes the previously displayed [ConnectionPointHighlight] from the [DrawingView]. */
    protected fun removePortViewHighlight(view: DrawingView<*>) {
        if (portViewHighlight != null) {
            LOG.trace("removePortViewHighlight")
            view.ghostContainer.remove(portViewHighlight!!)
            view.ghostContainer.validate()
            portViewHighlight = null
        }
    }
}