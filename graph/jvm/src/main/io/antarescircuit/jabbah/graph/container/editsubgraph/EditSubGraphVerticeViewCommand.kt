package io.antarescircuit.jabbah.graph.container.editsubgraph

import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.command.AbstractDrawingViewCommand
import io.antarescircuit.jabbah.graph.container.ContainerDrawing
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeView
import io.antarescircuit.jabbah.io.StorableCloner

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