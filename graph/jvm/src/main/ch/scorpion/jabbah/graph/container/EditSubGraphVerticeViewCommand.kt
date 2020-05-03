package ch.scorpion.jabbah.graph.container

import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.Undoable
import ch.scorpion.jabbah.edit.command.AbstractCommand
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import ch.scorpion.jabbah.io.StorableCloner

class EditSubGraphVerticeViewCommand(
	private val drawingView: DrawingView<*>,
	private val verticeViewId: Int,
	private val newDrawing: ContainerDrawing?
) : AbstractCommand("graph.command.editSubGraphVerticeView") {

	private val verticeView: SubGraphVerticeView<*> get() = drawingView.drawing.getWithId(verticeViewId) as SubGraphVerticeView<*>

	override fun execute() {
		verticeView.setEditedContainerDrawing(newDrawing?.let { StorableCloner.clone(it) })
	}

	override fun undo() {
		// TODO Delete
		//verticeView.setEditedContainerDrawing(oldDrawingClone)
	}
}