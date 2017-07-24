package ch.scorpion.jabbah.graph.view.net.edge

import ch.scorpion.jabbah.draw.InputEventHandler
import ch.scorpion.jabbah.edit.EditInputEventContext
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.geom.Point2D

/**
 * Supports dragging individual orthogonal segments of a {@link EdgeView}.
 */
class DragEdgeSegmentHandler : EdgeViewInputEventHandler() {

    private val LOG by logger(DragEdgeSegmentHandler::class)

    private var lastX: Double = 0.0
    private var lastY: Double = 0.0
    private var segmentIndex: Int? = null
    private var totalOffset: Double = 0.0

    /** ---- [InputEventHandler] */

    override fun mousePressed(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
        LOG.trace("mousePressed at (${context.x},${context.y})")
        lastX = context.x + context.editor.snapManager.snapX(context.x, context.y)
        lastY = context.y + context.editor.snapManager.snapY(context.x, context.y)
        segmentIndex = edgeView!!.findSegment(context.x, context.y)
        totalOffset = 0.0
        return this
    }

    override fun mouseDragged(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
        LOG.trace("mouseDragged to (${context.x},${context.y})")
        val newX = context.x + context.editor.snapManager.snapX(context.x, context.y)
        val newY = context.y + context.editor.snapManager.snapY(context.x, context.y)

        if (newX != lastX || newY != lastY) {
            val (segmentIndex1, offset) = edgeView!!.moveSegment(
                segmentIndex!!,
                Point2D(lastX, lastY),
                Point2D(newX, newY))
            lastX = newX
            lastY = newY

            segmentIndex = segmentIndex1
            totalOffset += offset

            edgeView?.validate()
        }
        return this
    }

    override fun mouseReleased(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
        LOG.trace("mouseReleased at " + Point2D(context.x, context.y))
        if (totalOffset != 0.0) {
            context.editor.commandManager.beginTransaction(MoveSegmentCommand(context.editor, edgeView!!, segmentIndex!!, totalOffset), register = true)
            context.editor.commandManager.commitTransaction()
        }
        return null
    }
}