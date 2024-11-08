package ch.scorpion.jabbah.graph.container.editsubgraph

import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.command.AbstractDrawingViewCommand
import ch.scorpion.jabbah.graph.app.AbstractGraphViewCommand
import ch.scorpion.jabbah.graph.container.ContainerDrawing
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import ch.scorpion.jabbah.io.StorableCloner

class EditSubGraphVerticeViewCommand(
	drawingView: DrawingView<*>,
	private val verticeViewId: Int,
	private val newDrawing: ContainerDrawing?
) : AbstractDrawingViewCommand("graph.command.editSubGraphVerticeView", drawingView) {

	private val verticeView: SubGraphVerticeView<*> get() = view.drawing.getWithId(verticeViewId) as SubGraphVerticeView<*>

	override fun execute() {
		verticeView.setEditedContainerDrawing(newDrawing?.let { StorableCloner.clone(it) })
	}
}