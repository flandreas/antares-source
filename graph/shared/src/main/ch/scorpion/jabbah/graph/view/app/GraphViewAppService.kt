package ch.scorpion.jabbah.graph.view.app

import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rotation
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.app.*
import ch.scorpion.jabbah.edit.model.CopyPasteService
import ch.scorpion.jabbah.edit.model.group.GroupComponent
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.graph.library.LibraryDirectory
import ch.scorpion.jabbah.graph.library.LibraryElement
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.app.AbstractGraphViewCommand
import ch.scorpion.jabbah.graph.model.GraphType
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRef
import ch.scorpion.jabbah.graph.view.*
import ch.scorpion.jabbah.graph.view.connect.GraphViewConnectService
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.oscilloscope.OscilloscopeView
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
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
	fun addGraphElementViewFromLibrary(
		libraryElement: LibraryElement,
		location: Point2D,
		rotation: Rotation,
		editor: Editor,
		customizer: ComponentCustomizer? = null
	): Component

	/**
	 * Extracts all currently selected [GraphElementView] as a new [MetaGraph]
	 * and stores in under the given [graphName] in [libraryDirectory]
	 */
	fun extractMetaGraph(
		graphName: TranslatableText,
		type: GraphType,
		drawingView: DrawingView<GraphView>,
		libraryDirectory: LibraryDirectory
	)
}

open class GraphViewAppServiceImpl(
	copyPasteService: CopyPasteService = EditModule.copyPasteService,
	commandManager: CommandManager = EditModule.commandManager,
	protected val connectService: GraphViewConnectService = GraphViewModule.graphViewConnectService
) : DrawingAppServiceImpl(copyPasteService, commandManager), GraphViewAppService {

	companion object {
		private val LOG by logger(GraphViewAppServiceImpl::class)
	}

	/** ---- [DrawingAppService] interface */

	override fun add(
		component: Component,
		drawingView: DrawingView<Drawing<Component>>,
		customizer: ComponentCustomizer?
	): Component {
		return if (drawingView.drawing !is GraphView || component is GraphElementView<*>) {
			super.add(component, drawingView, customizer)
		} else {
			super.add(GraphElementViewWrapper(component), drawingView, customizer)
		}
	}

	override fun customizeAddedComponent(component: Component, drawing: Drawing<*>) {
		super.customizeAddedComponent(component, drawing)
		if (component is SubGraphVerticeView<*>) {
			if (component.model is SubGraphVerticeRef) {
				(component.model as SubGraphVerticeRef).graphType = (drawing as GraphView).graph!!.type
			}
		}
	}

	override fun delete(components: List<Component>, drawingView: DrawingView<*>, cmdDescriptionKey: String?) {
		logComponentAction("Delete", components.map { it.id }, drawingView)

		val componentSet = expandDeleteBuddies(components, drawingView.drawing as Drawing<Component>)

		commandManager.beginTransaction(cmdDescriptionKey ?: "edit.command.delete", drawingView)

		for (component in componentSet) {
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
			componentSet
				.filter { drawingView.drawing.contains(it) }
				.map { possibleWrapper(it, drawingView.drawing).id }))

		commandManager.commitTransaction()

		if (componentSet.any { it is OscilloscopeView }) {
			GraphViewModule.oscilloscopeViewService.handleOscilloscopeDeleted(drawingView as DrawingView<GraphView>)
		}
	}

	override fun ungroup(component: GroupComponent, drawingView: DrawingView<Drawing<Component>>) {
		ungroupImpl(component, possibleWrapper(component, drawingView.drawing), drawingView)
	}

	/** ---- [GraphViewAppService] interface */

	override fun addGraphElementViewFromLibrary(
		libraryElement: LibraryElement,
		location: Point2D,
		rotation: Rotation,
		editor: Editor,
		customizer: ComponentCustomizer?
	): Component {
		val command = AddGraphElementViewFromLibraryCommand(
			editor,
			libraryElement,
			location,
			rotation,
			componentCustomizer = customizer?.let { ComponentCustomizerPair(it, this) } ?: this
		)

		commandManager.execute(command)
		val component = editor.view.drawing.getWithId(command.addedComponentId) as Component

		LOG.userTrail("Add Component ${component.id} '${libraryElement.name}' from library/project at $location")

		editor.view.selectionManager.deselectAll()
		editor.view.selectionManager.select(component)

		return component
	}

	override fun extractMetaGraph(graphName: TranslatableText, type: GraphType, drawingView: DrawingView<GraphView>, libraryDirectory: LibraryDirectory) {
		val componentIds = drawingView.selectionManager.selection.map { it.id }
		LOG.userTrail("Extract ${componentIds.size} Components into new Library MetaGraph '$graphName'")
		commandManager.execute(ExtractMetaGraphCommand(graphName, type, drawingView, componentIds, libraryDirectory))
	}

	/** ---- [GraphViewAppServiceImpl] */

	private fun unconnectDeletedVerticeView(verticeView: VerticeView<*>, drawingView: DrawingView<GraphView>) {
		LOG.trace("unconnectDeletedVerticeView for verticeView ${verticeView.id}")
		drawingView.drawing.getEdgeViews()
			.filter { ev -> ev.origin?.connectableView === verticeView }
			.forEach { ev -> commandManager.execute(UnconnectEdgeViewOriginCommand(drawingView, connectService, ev.id)) }
		drawingView.drawing.getEdgeViews()
			.filter { ev -> ev.destination?.connectableView === verticeView }
			.forEach { ev -> commandManager.execute(UnconnectEdgeViewDestinationCommand(drawingView, connectService, ev.id)) }
	}

	private fun unconnectDeletedEdgeView(edgeView: EdgeView<Any>, drawingView: DrawingView<GraphView>) {
		LOG.trace("unconnectDeletedEdgeView for verticeView ${edgeView.id}")
		commandManager.execute(UnconnectEdgeViewCommand(drawingView, connectService, edgeView.id))
	}

	private fun getWrapperOf(component: Component, drawing: Drawing<*>): GraphElementViewWrapper? {
		return drawing.drawables
			.filter { it is GraphElementViewWrapper && it.component === component }
			.map { it as GraphElementViewWrapper }
			.firstOrNull()
	}

	private fun possibleWrapper(component: Component, drawing: Drawing<*>): Component {
		return getWrapperOf(component, drawing) ?: component
	}
}

private class UnconnectEdgeViewCommand(
	drawingView: DrawingView<GraphView>,
	private val connectService: GraphViewConnectService,
	private val edgeViewId: Int
) : AbstractGraphViewCommand("graph.command.unconnectEdgeView", drawingView) {

	private val edgeView get() = drawingView.drawing.getWithId(edgeViewId) as EdgeView<Any>

	override fun execute() {
		connectService.unconnect(edgeView)
	}
}

private class UnconnectEdgeViewOriginCommand(
	drawingView: DrawingView<GraphView>,
	private val connectService: GraphViewConnectService,
	private val edgeViewId: Int
) : AbstractGraphViewCommand("graph.command.unconnectEdgeViewOrigin", drawingView) {

	private val edgeView get() = drawingView.drawing.getWithId(edgeViewId) as EdgeView<Any>

	override fun execute() {
		connectService.unconnectEdgeViewOrigin(edgeView)
	}
}

private class UnconnectEdgeViewDestinationCommand(
	drawingView: DrawingView<GraphView>,
	private val connectService: GraphViewConnectService,
	private val edgeViewId: Int
) : AbstractGraphViewCommand("graph.command.unconnectEdgeViewDestination", drawingView) {

	private val edgeView get() = drawingView.drawing.getWithId(edgeViewId) as EdgeView<Any>

	override fun execute() {
		connectService.unconnectEdgeViewDestination(edgeView)
	}
}

class ExtractMetaGraphCommand(
	private val graphName: TranslatableText,
	private val type: GraphType,
	drawingView: DrawingView<GraphView>,
	private val componentIds: Collection<Int>,
	private val libraryDirectory: LibraryDirectory
) : AbstractGraphViewCommand("graph.command.extractMetaGraph", drawingView) {

	private lateinit var uuid: UUID

	override fun execute() {
		uuid = GraphViewModule.metaGraphService.extractMetaGraph(graphName, type, drawingView, componentIds, libraryDirectory)
	}

	override fun notifyUndo() {
		with (libraryDirectory.library!!) {
			getContainerLibraryElement(this@ExtractMetaGraphCommand.uuid)?.let {
				libraryService.removeLibraryItem(this, it)
			}
		}
	}
}
