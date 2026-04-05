package io.antarescircuit.jabbah.graph.view.app

import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.geom.Rotation
import io.antarescircuit.jabbah.draw.drawable.Orientable
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.DragManager
import io.antarescircuit.jabbah.edit.app.ComponentCustomizer
import io.antarescircuit.jabbah.edit.command.AbstractCommand
import io.antarescircuit.jabbah.graph.library.LibraryElement
import io.antarescircuit.jabbah.graph.model.GraphElement
import io.antarescircuit.jabbah.graph.view.GraphElementView
import io.antarescircuit.jabbah.graph.view.GraphView

/**
 * Adds a new [GraphElementView] created by a [LibraryElement] to a [GraphView] and optionally
 * performs additional actions according to [DragManager.finishDrop].
 */
class AddGraphElementViewFromLibraryCommand(
	editor: Editor,
	private val libraryElement: LibraryElement,
	private val location: Point2D,
	private val rotation: Rotation,
	private val componentCustomizer: ComponentCustomizer
): AbstractCommand("edit.command.add", editor) {

	private val graphView: GraphView get() = editor!!.view.drawing as GraphView

	var addedComponentId: Int = 0
		private set

	override fun getDetailedDescription(): String {
		val component = graphView.getWithId(addedComponentId)!!
		return "${super.getDetailedDescription()} ${component::class.simpleName} $addedComponentId"
	}

	override fun execute() {
		val graphElementView = libraryElement.getNewInstance<GraphElement>()
		graphElementView.location = location
		if (graphElementView.useRotation) {
			graphElementView.rotation = rotation
		} else if (graphElementView is Orientable && graphElementView.useOrientation) {
			graphElementView.orientation = Direction.of(rotation)
		}
		graphView.add(graphElementView)
		addedComponentId = graphElementView.id
		val verticeView = graphView.getWithId(addedComponentId) as Component

		componentCustomizer.customizeAddedComponent(verticeView, graphView)

		editor!!.dragManager.finishDrop(verticeView).forEach { it.execute() }
	}
}