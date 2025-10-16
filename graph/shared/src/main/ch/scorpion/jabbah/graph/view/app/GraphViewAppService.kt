package ch.scorpion.jabbah.graph.view.app

import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rotation
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.app.ComponentCustomizer
import ch.scorpion.jabbah.edit.app.ComponentCustomizerPair
import ch.scorpion.jabbah.edit.app.DrawingAppService
import ch.scorpion.jabbah.edit.app.DrawingAppServiceImpl
import ch.scorpion.jabbah.edit.model.ComponentMessage
import ch.scorpion.jabbah.edit.model.ComponentMessageType
import ch.scorpion.jabbah.edit.model.CopyPasteService
import ch.scorpion.jabbah.edit.model.group.GroupComponent
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.app.AbstractGraphViewCommand
import ch.scorpion.jabbah.graph.library.LibraryDirectory
import ch.scorpion.jabbah.graph.library.LibraryElement
import ch.scorpion.jabbah.graph.model.GraphType
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRef
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphElementViewWrapper
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.connect.GraphViewConnectService
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
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
			val addedComponent = super.add(component, drawingView, customizer)
			LOG.userTrail("Add '${addedComponent.type}' ${addedComponent.id} at ${component.location}")
			addedComponent
		} else {
			val addedComponent = super.add(GraphElementViewWrapper(component), drawingView, customizer)
			LOG.userTrail("Add '${(addedComponent as GraphElementViewWrapper).component?.type}' ${addedComponent.id} at ${component.location}")
			addedComponent
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

	/**
	 * Overwritten to reject grouping of non-graphical [Component Components] like [VerticeView VerticeViews],
	 * because it's not clear how to deal with the models in a group, and the base grouping implementation
	 * unconnects grouped [EdgeView EdgeViews].
	 */
	override fun group(components: List<Component>, drawingView: DrawingView<Drawing<Component>>) {
		if (components.any { !canGroup(it) }) {
			eventBus.post(ComponentMessage(
				ComponentMessageType.Error,
				components.first(),
				"graph.action.group.denied.msg"
			))
			return
		}
		return super.group(components, drawingView)
	}

	private fun canGroup(component: Component): Boolean = component is GraphElementViewWrapper

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
