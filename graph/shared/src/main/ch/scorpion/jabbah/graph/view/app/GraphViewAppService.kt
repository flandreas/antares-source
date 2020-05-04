package ch.scorpion.jabbah.graph.view.app

import ch.scorpion.jabbah.base.collection.Pair
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.app.DeleteCommand
import ch.scorpion.jabbah.edit.app.DrawingAppService
import ch.scorpion.jabbah.edit.app.DrawingAppServiceImpl
import ch.scorpion.jabbah.edit.command.AbstractCommand
import ch.scorpion.jabbah.edit.model.CopyPasteService
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.graph.library.LibraryElement
import ch.scorpion.jabbah.graph.view.*
import ch.scorpion.jabbah.graph.view.connect.GraphViewConnectService
import ch.scorpion.jabbah.graph.view.connect.JoinEdgeViewsResult
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.io.StorableCloner

/**
 * An application service for [GraphView] that enhances the domain services with undo/redo functionality
 * using [Command]s.
 */
interface GraphViewAppService : DrawingAppService {

	/**
	 * Add a [GraphElementView] to the [GraphView] in the specified [DrawingView] by creating a new instance using
	 * [LibraryElement]. This is necessary because [add] creates a clone using [StorableCloner], which doesn't work
	 * for [GraphElementView] (the model would be cloned).
	 */
	fun addGraphElementViewFromLibrary(libraryElement: LibraryElement, location: Point2D, drawingView: DrawingView<GraphView>): Component
}

open class GraphViewAppServiceImpl(
	copyPasteService: CopyPasteService = EditModule.copyPasteService,
	commandManager: CommandManager = EditModule.commandManager,
	private val connectService: GraphViewConnectService = GraphViewModule.graphViewConnectService
) : DrawingAppServiceImpl(copyPasteService, commandManager), GraphViewAppService {

	companion object {
		private val LOG by logger(GraphViewAppServiceImpl::class)
	}

	/** ---- [DrawingAppService] interface */

	override fun add(component: Component, drawingView: DrawingView<Drawing<Component>>): Component {
		return if (drawingView.drawing !is GraphView || component is GraphElementView<*>) {
			super.add(component, drawingView)
		} else {
			super.add(GraphElementViewWrapper(component), drawingView)
		}
	}

	override fun delete(components: List<Component>, drawingView: DrawingView<Drawing<Component>>, cmdDescriptionKey: String?) {
		LOG.debug("delete ${components.size} components")

		commandManager.beginTransaction(cmdDescriptionKey ?: "edit.command.delete", drawingView)

		for (component in components) {
			if (component is VerticeView<*>) {
				unconnectDeletedVerticeView(component, drawingView as DrawingView<GraphView>)
			} else if (component is EdgeView<*>) {
				if (drawingView.drawing.contains(component)) {
					// Might have been joined away by a previous removal of another EdgeView
					unconnectDeletedEdgeView(component as EdgeView<Any>, drawingView as DrawingView<GraphView>)
				}
			}
		}

		commandManager.execute(DeleteCommand(
			drawingView,
			components
				.filter { drawingView.drawing.contains(it) }
				.map { possibleWrapper(it, drawingView.drawing).id }))

		commandManager.commitTransaction()
	}

	/** ---- [GraphViewAppService] interface */

	override fun addGraphElementViewFromLibrary(libraryElement: LibraryElement, location: Point2D, drawingView: DrawingView<GraphView>): Component {
		LOG.debug("Add Component from LibraryElement ${libraryElement.name}")

		val command = AddGraphElementViewFromLibraryCommand(drawingView, libraryElement, location)
		commandManager.execute(command)
		val component = drawingView.drawing.getWithId(command.addedComponentId) as Component

		drawingView.selectionManager.deselectAll()
		drawingView.selectionManager.select(component)

		return component
	}

	/** ---- [GraphViewAppServiceImpl] */

	private fun unconnectDeletedVerticeView(verticeView: VerticeView<*>, drawingView: DrawingView<GraphView>) {
		LOG.debug("unconnectDeletedVerticeView for verticeView ${verticeView.id}")
		drawingView.drawing.getEdgeViews()
			.filter { ev -> ev.origin?.connectableView === verticeView }
			.forEach { ev -> commandManager.execute(UnconnectEdgeViewOriginCommand(drawingView, connectService, ev.id)) }
		drawingView.drawing.getEdgeViews()
			.filter { ev -> ev.destination?.connectableView === verticeView }
			.forEach { ev -> commandManager.execute(UnconnectEdgeViewDestinationCommand(drawingView, connectService, ev.id)) }
	}

	private fun unconnectDeletedEdgeView(edgeView: EdgeView<Any>, drawingView: DrawingView<GraphView>) {
		LOG.debug("unconnectDeletedEdgeView for verticeView ${edgeView.id}")
		commandManager.execute(UnconnectEdgeViewCommand(drawingView, connectService, edgeView.id))
	}

	private fun getWrapperOf(component: Component, drawing: Drawing<Component>): GraphElementViewWrapper? {
		return drawing.getDrawables()
			.filter { it is GraphElementViewWrapper && it.component === component }
			.map { it as GraphElementViewWrapper }
			.firstOrNull()
	}

	private fun possibleWrapper(component: Component, drawing: Drawing<Component>): Component {
		return getWrapperOf(component, drawing) ?: component
	}
}

private class UnconnectEdgeViewCommand(
	private val drawingView: DrawingView<GraphView>,
	private val connectService: GraphViewConnectService,
	private val edgeViewId: Int
) : AbstractCommand("graph.command.unconnectEdgeView", null) {

	private val edgeView get() = drawingView.drawing.getWithId(edgeViewId) as EdgeView<Any>

	lateinit var joinResults: Pair<JoinEdgeViewsResult<Any>?>
		private set

	override fun execute() {
		joinResults = connectService.unconnect(edgeView)
	}
}

private class UnconnectEdgeViewOriginCommand(
	private val drawingView: DrawingView<GraphView>,
	private val connectService: GraphViewConnectService,
	private val edgeViewId: Int
) : AbstractCommand("graph.command.unconnectEdgeViewOrigin", null) {

	private val edgeView get() = drawingView.drawing.getWithId(edgeViewId) as EdgeView<Any>

	override fun execute() {
		connectService.unconnectEdgeViewOrigin(edgeView)
	}
}

private class UnconnectEdgeViewDestinationCommand(
	private val drawingView: DrawingView<GraphView>,
	private val connectService: GraphViewConnectService,
	private val edgeViewId: Int
) : AbstractCommand("graph.command.unconnectEdgeViewDestination", null) {

	private val edgeView get() = drawingView.drawing.getWithId(edgeViewId) as EdgeView<Any>

	override fun execute() {
		connectService.unconnectEdgeViewDestination(edgeView)
	}
}
