package ch.scorpion.jabbah.graph.view.net.edge

import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.command.AbstractCommand
import ch.scorpion.jabbah.graph.view.EdgeView

/**
 * Moves an individual segment of an [EdgeView].
 *
 * Must not be undoable because the [EdgeView]'s layout might be changed by other operations such as
 * delete or move, making [segmentIndex] invalid if [MoveSegmentCommand] was undoable.
 * This problem is avoided if [MoveSegmentCommand] is always replayed from snapshots in undo/redo scenarios.
 * See issue #713 on GitHub.
 */
class MoveSegmentCommand(
	editor: Editor,
	private val edgeViewId: Int,
	private val segmentIndex: Int,
	private val offset: Double
) : AbstractCommand("graph.command.moveSegment", editor) {

	private val edgeView get() = editor!!.drawing.getWithId(edgeViewId) as EdgeView<*>

	override fun execute() {
		edgeView.moveSegment(segmentIndex, offset)
	}
}
