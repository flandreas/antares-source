package ch.scorpion.jabbah.graph.view.app

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.DragManager
import ch.scorpion.jabbah.edit.command.AbstractCommand
import ch.scorpion.jabbah.graph.library.LibraryElement
import ch.scorpion.jabbah.graph.model.GraphElement
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.VerticeView

/**
 * Adds a new [GraphElementView] created by a [LibraryElement] to a [GraphView] and optionally
 * performs additional actions according to [DragManager.finishDrop].
 */
class AddGraphElementViewFromLibraryCommand(
	editor: Editor,
	private val libraryElement: LibraryElement,
	private val location: Point2D,
	private val componentCustomizer: (Component) -> Unit = {}
): AbstractCommand("edit.command.add", editor) {

	private val graphView: GraphView get() = editor!!.view.drawing as GraphView

	var addedComponentId: Int = 0
		private set

	override fun execute() {
		val graphElementView = libraryElement.getNewInstance<GraphElement>()
		graphElementView.location = location
		graphView.add(graphElementView)
		addedComponentId = graphElementView.id
		val verticeView = graphView.getWithId(addedComponentId) as VerticeView

		componentCustomizer.invoke(verticeView)

		editor!!.dragManager.finishDrop(verticeView).forEach { it.execute() }
	}
}