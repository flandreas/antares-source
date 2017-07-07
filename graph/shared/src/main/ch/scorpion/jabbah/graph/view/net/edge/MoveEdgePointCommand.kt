package ch.scorpion.jabbah.graph.view.net.edge

import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.command.AbstractCommand
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.graph.view.EdgeView

/** Moves an individual [Point2D] of an [EdgeView]. */
class MoveEdgePointCommand(
    editor: Editor,
    private val edgeView: EdgeView<*>,
    private val pointIndex: Int,
    private val offset: Point2D
) : AbstractCommand("graph.command.moveEdgePoint", editor) {

    override fun execute() {
        val p = edgeView.getSegmentPoint(pointIndex)
        edgeView.movePoint(pointIndex, p.x + offset.x, p.y + offset.y)
    }

    override fun undo() {
        val p = edgeView.getSegmentPoint(pointIndex)
        edgeView.movePoint(pointIndex, p.x - offset.x, p.y - offset.y)
    }
}