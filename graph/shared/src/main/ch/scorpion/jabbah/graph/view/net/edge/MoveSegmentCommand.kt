package ch.scorpion.jabbah.graph.view.net.edge

import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.Undoable
import ch.scorpion.jabbah.edit.command.AbstractCommand
import ch.scorpion.jabbah.graph.view.EdgeView

/**
 * Moves an individual segment of an [EdgeView].
 */
class MoveSegmentCommand(
	editor: Editor,
	private val edgeViewId: Int,
	origSegmentIndex: Int,
	private val offset: Double
) : AbstractCommand("graph.command.moveSegment", editor), Undoable {

	private var currentSegmentIndex = origSegmentIndex

	private val edgeView get() = editor!!.drawing.getWithId(edgeViewId) as EdgeView<*>

	override fun execute() {
		val moveSegmentInfo = edgeView.moveSegment(currentSegmentIndex, offset)
		currentSegmentIndex = moveSegmentInfo.segmentIndex
	}

	override fun undo() {
		val moveSegmentInfo = edgeView.moveSegment(currentSegmentIndex, -offset)
		currentSegmentIndex = moveSegmentInfo.segmentIndex
	}
}
