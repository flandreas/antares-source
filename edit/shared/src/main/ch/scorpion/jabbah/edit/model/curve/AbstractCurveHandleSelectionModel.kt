package ch.scorpion.jabbah.edit.model.curve

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.draw.InputEventHandler
import ch.scorpion.jabbah.draw.InputEventHandlerAdapter
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.draw.graphics.Cursor
import ch.scorpion.jabbah.draw.graphics.Stroke
import ch.scorpion.jabbah.edit.EditInputEventContext
import ch.scorpion.jabbah.edit.SelectionModel
import ch.scorpion.jabbah.edit.select.AbstractHandleSelectionModel
import ch.scorpion.jabbah.edit.select.Handle
import ch.scorpion.jabbah.edit.select.RectangularHandle
import kotlin.jvm.JvmStatic

/**
 * A [SelectionModel] consisting of [Handle]s to be used for selecting and shaping an [AbstractCurveComponent].
 */
abstract class AbstractCurveHandleSelectionModel<T: AbstractCurveComponent>(
    c: T
) : AbstractHandleSelectionModel<T>(c) {

    companion object {
        private val LOG by logger(AbstractCurveHandleSelectionModel::class)

        @JvmStatic
        protected val TANGENT_STROKE = Stroke(0.5f, dash = floatArrayOf(2.0f, 4.0f))
    }

    /** Handles input events on an individual [Handle].*/
    private val pointHandler = QuadCurveInputEventHandler()

    /** ---- [AbstractHandleSelectionModel] */

    override fun createInputEventHandler(): InputEventHandler<EditInputEventContext> = CurveEventHandler()

    override fun updateHandlesImpl() {
        if (requiredHandlesCount != calculateRequiredHandlesCount()) {
            createHandles()
        }
        for (i in 0 until calculateRequiredHandlesCount()) {
            getHandle(i).location = component.getPointAt(i)
        }
    }

    private fun createHandles() {
        clearHandles()
        for (i in 0 until calculateRequiredHandlesCount()) {
            addHandle(RectangularHandle(pointHandler))
        }
        requiredHandlesCount = calculateRequiredHandlesCount()
    }

    private inner class CurveEventHandler : EventHandler() {
        private var oldLocation = Point2D.ZERO

        override fun dragHandleBegin(view: View<*>) {
            val index = getIndexOf(focusHandle!!)
            oldLocation = Point2D(component.getPointAt(index))
        }

        override fun dragHandleEnd(context: EditInputEventContext) {
            val index = getIndexOf(focusHandle!!)
            val newLocation = component.getPointAt(index)
            LOG.userTrail("Move point $index of '${component.type}' ${component.id} to $newLocation")
            context.editor.commandManager.register(
                MoveCurvePointCommand(context.editor.view, component.id, index, oldLocation, newLocation))
        }
    }

    private inner class QuadCurveInputEventHandler : InputEventHandlerAdapter<EditInputEventContext>() {
        override fun mouseMoved(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
            context.view.setCursor(Cursor.CROSSHAIR)
            return this
        }

        override fun mouseDragged(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
            component.setPointAt(getIndexOf((this@AbstractCurveHandleSelectionModel.inputEventHandler as AbstractCurveHandleSelectionModel<*>.CurveEventHandler).focusHandle!!), Point2D(context.x, context.y))
            return this
        }
    }
}