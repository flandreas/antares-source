package ch.scorpion.jabbah.graph.view.app

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.Undoable
import ch.scorpion.jabbah.edit.command.AbstractCommand
import ch.scorpion.jabbah.graph.library.LibraryElement
import ch.scorpion.jabbah.graph.model.GraphElement
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphView

/** Adds a new [GraphElementView] created by a [LibraryElement] to a [GraphView]. */
class AddGraphElementViewFromLibraryCommand(
	private val drawingView: DrawingView<GraphView>,
	private val libraryElement: LibraryElement,
	private val location: Point2D
): AbstractCommand("edit.command.add"), Undoable {

	private val graphView: GraphView get() = drawingView.drawing
	var addedComponentId: Int = 0
		private set

	override fun execute() {
		val graphElementView = libraryElement.getNewInstance<GraphElement>()
		graphElementView.location = location
		drawingView.drawing.add(graphElementView)
		addedComponentId = graphElementView.id
	}

	override fun undo() {
		graphView.remove(graphView.getWithId(addedComponentId) as GraphElementView<*>)
	}
}