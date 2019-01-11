package ch.scorpion.jabbah.graph.container

import ch.scorpion.jabbah.edit.command.AbstractCommand
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView

class EditSubGraphVerticeViewCommand(
	private val verticeView: SubGraphVerticeView<*>,
	private val newDrawing: ContainerDrawing?
) : AbstractCommand("graph.command.editSubGraphVerticeView") {

	private val oldDrawing: ContainerDrawing = verticeView.getEditableContainerDrawing()

	override fun execute() {
		verticeView.setEditedContainerDrawing(newDrawing)
	}

	override fun undo() {
		verticeView.setEditedContainerDrawing(oldDrawing)
	}
}