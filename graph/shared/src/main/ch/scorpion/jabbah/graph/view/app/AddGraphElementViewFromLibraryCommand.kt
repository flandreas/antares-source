package ch.scorpion.jabbah.graph.view.app

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.edit.Command
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.command.AbstractCommand
import ch.scorpion.jabbah.graph.library.LibraryElement
import ch.scorpion.jabbah.graph.model.GraphElement
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.VerticeView
import ch.scorpion.jabbah.graph.view.connect.GraphViewConnectService
import ch.scorpion.jabbah.graph.view.editor.AutoConnector
import ch.scorpion.jabbah.graph.view.module.GraphViewModule

/**
 * Adds a new [GraphElementView] created by a [LibraryElement] to a [GraphView] and optionally
 * connects it with open-ended [EdgeView]s.
 */
class AddGraphElementViewFromLibraryCommand(
	editor: Editor,
	private val libraryElement: LibraryElement,
	private val location: Point2D,
	private val service: GraphViewConnectService = GraphViewModule.graphViewConnectService
): AbstractCommand("edit.command.add", editor) {

	private val graphView: GraphView get() = editor!!.view.drawing as GraphView
	private var connectCommands: Collection<Command>? = null

	var addedComponentId: Int = 0
		private set

	override fun execute() {
		val graphElementView = libraryElement.getNewInstance<GraphElement>()
		graphElementView.location = location
		graphView.add(graphElementView)
		addedComponentId = graphElementView.id

		if (connectCommands == null) {
			val verticeView = graphView.getWithId(addedComponentId) as VerticeView
			connectCommands = AutoConnector.getAutoConnectCommands(editor!!, verticeView, service)
			AutoConnector.handleDragFinished()
		}

		connectCommands!!.forEach { it.execute() }
	}
}