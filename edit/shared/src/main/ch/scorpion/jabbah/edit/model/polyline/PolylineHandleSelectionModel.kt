package ch.scorpion.jabbah.edit.model.polyline

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.draw.InputEventHandler
import ch.scorpion.jabbah.draw.InputEventHandlerAdapter
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.draw.graphics.Cursor
import ch.scorpion.jabbah.edit.Command
import ch.scorpion.jabbah.edit.EditInputEventContext
import ch.scorpion.jabbah.edit.SelectionModel
import ch.scorpion.jabbah.edit.select.AbstractHandleSelectionModel
import ch.scorpion.jabbah.edit.select.Handle
import ch.scorpion.jabbah.edit.select.RectangularHandle

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
            val newLocation = component.getPointAt(index)

	        component.compact()

	        val command = if (component.pointsCount != oldPointsCount) {
		        JoinPolylinePointsCommand(context.editor, component.id, index, oldLocation)
	        } else {
		        MovePolylinePointCommand(context.editor, component.id, index, newLocation)
	        }

	        context.editor.commandManager.register(command as Command)
        }

        /** Adds an additional [Point2D] by double-clicking.*/
        override fun mouseClicked(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
            if (context.mouseEvent!!.clickCount == 2) {
                component.findSegment(context.x, context.y, 10)?.let {
                    LOG.debug("Add handle $it")
                    val snap = context.editor.snapManager.snap(context.x, context.y)
                    context.editor.commandManager.execute(
                        AddPolylinePointCommand(context.editor, component.id, it + 1, snap.add(context.location)))
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