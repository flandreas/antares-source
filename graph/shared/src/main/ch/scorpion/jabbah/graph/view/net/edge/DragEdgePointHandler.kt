package ch.scorpion.jabbah.graph.view.net.edge

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.draw.InputEventHandler
import ch.scorpion.jabbah.draw.graphics.Cursor
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.EditInputEventContext
import ch.scorpion.jabbah.graph.view.EdgeView

/**
 * Supports dragging a segment point of an [EdgeView] when [LayoutType.NONE] is active.
 */
class DragEdgePointHandler : EdgeViewInputEventHandler() {

    companion object {
        private val LOG by logger(DragEdgePointHandler::class)
    }

    private var highlight: DragEdgePointHighlight? = null

    private var oldLocation: Point2D? = null

	/** ---- [EdgeViewInputEventHandler] */

	override fun dismiss(view: DrawingView<*>) {
		removeHighlight(view)
	}

    /** ---- [InputEventHandler] */

    override fun mouseMoved(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
        LOG.trace("mouseMoved")
        if (edgeView!!.contains(context.x, context.y)) {
            if (highlight == null) {
                displayHighlight(context.drawingView)
            }

            highlight!!.updateMouseLocation(context.x, context.y)
            if (highlight!!.pointIndex != null) {
                context.view.setCursor(Cursor.CROSSHAIR)
            } else {
                context.view.setCursor(Cursor.DEFAULT)
            }
            return this
        }

        removeHighlight(context.drawingView)
        return null
    }

    override fun mousePressed(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
        if (highlight?.pointIndex != null) {
            oldLocation = edgeView!!.getSegmentPoint(highlight!!.pointIndex!!)
            return this
        }
        return null
    }

    override fun mouseDragged(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
        if (oldLocation != null) {
            highlight?.invalidate()

            val snap = context.editor.snapManager.snap(context.x, context.y)

            edgeView!!.movePoint(highlight!!.pointIndex!!, context.x + snap.x, context.y + snap.y)
            edgeView!!.validate()
            highlight?.validate()
            return this
        }
        return null
    }

    override fun mouseReleased(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
        if (oldLocation != null) {
            val newLocation = edgeView!!.getSegmentPoint(highlight!!.pointIndex!!)
            LOG.userTrail("Move point ${highlight!!.pointIndex!!} of EdgeView ${edgeView!!.id} to $newLocation")
            context.editor.commandManager.register(MoveEdgePointCommand(
                    context.editor, edgeView!!.id, highlight!!.pointIndex!!, newLocation.subtract(oldLocation!!)))
        }
        oldLocation = null
        return null
    }

    /** ---- [DragEdgePointHandler] */

    private fun displayHighlight(view: DrawingView<*>) {
        LOG.trace("displayHighlight")
        if (highlight == null) {
            highlight = DragEdgePointHighlight(edgeView!!)
            view.ghostContainer.add(highlight!!)
        }
        highlight?.validate()
    }

    private fun removeHighlight(view: DrawingView<*>) {
        LOG.trace("removeHighlight")
        if (highlight != null) {
            view.ghostContainer.remove(highlight!!)
            view.ghostContainer.validate()
            highlight = null
        }
    }
}