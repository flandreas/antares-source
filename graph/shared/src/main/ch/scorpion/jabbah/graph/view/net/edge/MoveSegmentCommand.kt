package ch.scorpion.jabbah.graph.view.net.edge

import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.command.AbstractCommand
import ch.scorpion.jabbah.graph.view.EdgeView

/**
 * Moves an individual segment of an [EdgeView].
 */
class MoveSegmentCommand(
        editor: Editor,
        private val edgeView: EdgeView<*>,
        private var segmentIndex: Int,
        private val offset: Double
) : AbstractCommand("graph.command.moveSegment", editor) {

    override fun execute() {
        val moveSegmentInfo = edgeView.moveSegment(segmentIndex, offset)
        segmentIndex = moveSegmentInfo.segmentIndex
    }

    override fun undo() {
        val moveSegmentInfo = edgeView.moveSegment(segmentIndex, -offset)
        segmentIndex = moveSegmentInfo.segmentIndex
    }
}
