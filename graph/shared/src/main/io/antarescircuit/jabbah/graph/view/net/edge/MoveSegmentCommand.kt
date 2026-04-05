package io.antarescircuit.jabbah.graph.view.net.edge

import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.command.AbstractCommand
import io.antarescircuit.jabbah.graph.view.EdgeView

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

	override fun getDetailedDescription(): String =
		"${super.getDetailedDescription()} $edgeViewId segmentIndex:$segmentIndex offset:$offset"

	override fun execute() {
		edgeView.moveSegment(segmentIndex, offset)
	}
}
