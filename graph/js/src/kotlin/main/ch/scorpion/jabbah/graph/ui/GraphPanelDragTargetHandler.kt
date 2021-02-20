package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.ui.ComponentDragTargetHandler
import ch.scorpion.jabbah.edit.ui.DragAndDropDepo

class GraphPanelDragTargetHandler(editor: Editor) : ComponentDragTargetHandler(editor) {

	override fun extractTransferData(): Any? {
		val data = DragAndDropDepo.data
		if (data is GraphElementViewTransferableData) {
			return data.graphElementView
		}
		return data
	}
}