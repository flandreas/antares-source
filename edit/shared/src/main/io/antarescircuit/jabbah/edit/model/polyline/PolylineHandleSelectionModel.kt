package io.antarescircuit.jabbah.edit.model.polyline

import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.draw.InputEventHandler
import io.antarescircuit.jabbah.draw.InputEventHandlerAdapter
import io.antarescircuit.jabbah.draw.View
import io.antarescircuit.jabbah.draw.graphics.Cursor
import io.antarescircuit.jabbah.edit.Command
import io.antarescircuit.jabbah.edit.EditInputEventContext
import io.antarescircuit.jabbah.edit.SelectionModel
import io.antarescircuit.jabbah.edit.select.AbstractHandleSelectionModel
import io.antarescircuit.jabbah.edit.select.Handle
import io.antarescircuit.jabbah.edit.select.RectangularHandle

/**
 * A [SelectionModel] consisting of [Handle]s to be used for selecting and shaping a [PolylineComponent].
 */
class PolylineHandleSelectionModel(c: PolylineComponent) : AbstractHandleSelectionModel<PolylineComponent>(c) {

	companion object {
        private val LOG by logger(PolylineHandleSelectionModel::class)
	}

    /** Handles input events on an individual [Handle].*/
    private val pointHandler = PolylinePointInputEventHandler()

    /** ---- [AbstractHandleSelectionModel] */

    override fun createInputEventHandler(): InputEventHandler<EditInputEventContext> {
        return PolylineEventHandler()
    }

    override fun calculateRequiredHandlesCount(): Int {
        return component.pointsCount
    }

    override fun updateHandlesImpl() {
        if (requiredHandlesCount != calculateRequiredHandlesCount()) {
            createHandles()
        }
        for (i in 0 until component.pointsCount) {
            getHandle(i).setLocation(component.getPointAt(i).x, component.getPointAt(i).y)
        }
    }

    /** ---- [PolylineHandleSelectionModel] */

    private fun createHandles() {
        clearHandles()
        for (i in 0 until component.pointsCount) {
            addHandle(RectangularHandle(pointHandler))
        }
        requiredHandlesCount = component.pointsCount
    }

    /**
     * Handles input events by dispatching them to the appropriate [Handle] and manages overall changes of the
     * selected [PolylineComponent] by creating an command after the mouse has been released.
     */
    private inner class PolylineEventHandler : EventHandler() {
        private var oldLocation = Point2D.ZERO
	    private var oldPointsCount: Int = 0

        override fun dragHandleBegin(view: View<*>) {
            val index = getIndexOf(focusHandle!!)
            oldLocation = Point2D(component.getPointAt(index))
	        oldPointsCount = component.pointsCount
        }

        override fun dragHandleEnd(context: EditInputEventContext) {
            val index = getIndexOf(focusHandle!!)

	        component.compact()

	        val command: Command = if (component.pointsCount != oldPointsCount) {
                LOG.userTrail("Join point $index of polyline ${component.id} at $oldLocation")
		        JoinPolylinePointsCommand(context.editor, component.id, index, oldLocation)
	        } else {
                LOG.userTrail("Move point $index of polyline ${component.id} to $oldLocation")
		        MovePolylinePointCommand.forOldLocation(context.editor, component, index, oldLocation)
	        }

	        context.editor.commandManager.register(command)
        }

        /** Adds an additional [Point2D] by double-clicking.*/
        override fun mouseClicked(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
            if (context.mouseEvent?.clickCount == 2) {
                component.findSegment(context.x, context.y, 10)?.let {
                    LOG.trace("Add handle $it")
                    val snap = context.editor.snapManager.snap(context.x, context.y)
                    val location = snap.add(context.location)
                    LOG.userTrail("Add point ${it + 1} to polyline ${component.id} at $location")
                    context.editor.commandManager.execute(
                        AddPolylinePointCommand(context.editor, component.id, it + 1, location))
                }
            }
            return null
        }
    }

    /** Handles input event on an individual [Handle].*/
    private inner class PolylinePointInputEventHandler : InputEventHandlerAdapter<EditInputEventContext>() {

        override fun mouseMoved(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
            context.view.setCursor(Cursor.CROSSHAIR)
            return this
        }

        override fun mouseDragged(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
            component.setPointAt(getIndexOf((this@PolylineHandleSelectionModel.inputEventHandler as PolylineEventHandler).focusHandle!!), context.x, context.y)
            return this
        }
    }
}