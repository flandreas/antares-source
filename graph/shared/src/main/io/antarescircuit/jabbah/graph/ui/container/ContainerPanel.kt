package io.antarescircuit.jabbah.graph.ui.container

import io.antarescircuit.jabbah.app.ApplicationDataContentEvent
import io.antarescircuit.jabbah.app.ApplicationDataEvent
import io.antarescircuit.jabbah.base.AbstractAction
import io.antarescircuit.jabbah.base.Action
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.event.EventHandler
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.ui.AbstractUIController
import io.antarescircuit.jabbah.base.ui.UIView
import io.antarescircuit.jabbah.edit.*
import io.antarescircuit.jabbah.edit.command.AbstractCommand
import io.antarescircuit.jabbah.edit.module.EditModule
import io.antarescircuit.jabbah.edit.properties.ComponentPropertyPanelController
import io.antarescircuit.jabbah.graph.GraphApplicationContextHolder
import io.antarescircuit.jabbah.graph.MetaGraph
import io.antarescircuit.jabbah.graph.container.ContainerDrawing
import io.antarescircuit.jabbah.graph.container.isManualContainer
import io.antarescircuit.jabbah.graph.ui.GraphFrameController
import io.antarescircuit.jabbah.graph.view.GraphElementView
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeView

/**
 * A [UIView] for editing the outside [ContainerDrawing] of a [GraphView].
 */
interface ContainerPanelView : UIView {

	val rightSidebarOpen: Boolean

	fun dataChanged()

	fun updateIsManualContainer(isManualContainer: Boolean)

	fun conformGenerateContainerDrawing(): Boolean

	fun generateContainerDrawing()

	fun activeChanged()
}

class ContainerPanelController(
	applicationContextHolder: GraphApplicationContextHolder,
	displayGlobalMessages: Boolean = true,
	val mainGraphDrawingView: DrawingView<GraphElementView<*>, GraphView>,
	private val eventBus: EventBus = BaseModule.eventBus
) : AbstractUIController<ContainerPanelView>() {

	companion object {
		private val LOG by logger(ContainerPanelController::class)
	}

	val drawingView: DrawingView<Component, Drawing<Component>> = EditModule.drawingViewFactory.create(
		ContainerDrawing(),
		applicationContextHolder,
		displayGlobalMessages,
		GraphFrameController.CONTAINER_EDITOR_NAME)

	val editor = GraphViewModule.containerEditorFactory(drawingView, mainGraphDrawingView)

	val propertyPanelController = ComponentPropertyPanelController(editor, eventBus)

	val generateContainerAction: Action = GenerateContainerAction()

	var editable: Boolean = true
		private set

	val symbolComparatorController = SymbolComparatorController(drawingView)

	private val applicationDataEventHandler: EventHandler<ApplicationDataEvent> = { handle(it) }

	private val applicationDataContentEventHandler: EventHandler<ApplicationDataContentEvent> = { handle(it) }

	private val commandHandler: EventHandler<CommandEvent> = { handle(it) }

	// TODO: This is always the GraphView within mainGraphDrawingView. Why the property?
	var graphView: GraphView? = null
		private set

	var containerDrawing: ContainerDrawing? = null
		private set

	/** The "manual Container" property in its persistent state, i.e. when read from store. */
	private var isManualContainerOrig: Boolean = false
		set(value) {
			field = value
			view.updateIsManualContainer(field)
		}

	/** The current "manual Container" property, i.e. after symbol has possibly been changed by the user. */
	private var isManualContainerCurrent: Boolean = false
		set(value) {
			field = value
			view.updateIsManualContainer(field)
		}

	var active: Boolean = false
		set(value) {
			LOG.debug("Setting ContainerPanel to active=$value")
			if (field != value) {
				field = value
				// Update the UI of the JTree in order to recalculate the width of the TreeRenderer's JLabels,
				// which are obviously cached by the JTree's UIManager. The tree nodes display the names of domain object,
				// and these names might have been changed while the ContainerPanel wasn't active
				view.activeChanged()
				editor.active = value && editable
				updateSymbolComparatorActiveness()
			}
		}

	init {
		eventBus.register(ApplicationDataEvent::class, applicationDataEventHandler)
		eventBus.register(ApplicationDataContentEvent::class, applicationDataContentEventHandler)
		eventBus.register(CommandEvent::class, commandHandler)
	}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(applicationDataEventHandler)
		eventBus.unregister(applicationDataContentEventHandler)
		eventBus.unregister(commandHandler)
		editor.dispose()
		propertyPanelController.dispose()
		symbolComparatorController.dispose()
	}

	/** ---- [ContainerPanelController] */

	/**
	 * Sets the data to be displayed by this [ContainerPanelController]. This method is used if this
	 * [ContainerPanelController] is NOT used for the main application data (in which case it's [ContainerDrawing]
	 * would be indirectly set as of [ApplicationDataEvent]), but in additional / separate context,
	 * e.g. when editing the symbol of a [SubGraphVerticeView].
	 *
	 * @param graphView the main [GraphView] in the editable main panel
	 * @param containerDrawing the [ContainerDrawing] that represents the outer view of `graphView`
	 * @param editable `true` if the user is authorized to edit the [ContainerDrawing]
	 */
	fun setData(
		graphView: GraphView,
		containerDrawing: ContainerDrawing,
		editable: Boolean,
		isManualContainer: Boolean,
		applyZoomStrategy: Boolean = true
	) {
		this.editable = editable
		this.isManualContainerOrig = isManualContainer
		editor.view.setDrawing(containerDrawing, applyZoomStrategy)
		this.graphView = graphView
		this.containerDrawing = containerDrawing

		notifyViewUpdate()
	}

	private fun clearData() {
		editable = false
		isManualContainerOrig = false
		// Make sure the former ContainerDrawing is gone
		editor.view.setDrawing(ContainerDrawing(), false)
		graphView = null
		containerDrawing = null

		notifyViewUpdate()
	}

	/**
	 * Similar to [setData], but used in the context of [UndoableDataHolder].
	 */
	fun updateData(containerDrawing: ContainerDrawing) {
		editor.view.setDrawing(containerDrawing)
	}

	fun generateContainerDrawing() {
		view.generateContainerDrawing()
		isManualContainerCurrent = false
	}

	fun handleRightSidebarOpen(open: Boolean) {
		LOG.debug("rightSidebarPane changed to open = $open")
		updateSymbolComparatorActiveness()
	}

	private fun updateSymbolComparatorActiveness() {
		symbolComparatorController.active = view.rightSidebarOpen && active
	}

	private fun handle(event: ApplicationDataEvent) {
		if (event.newData == null) {
			clearData()

		} else if (event.newData?.content is MetaGraph) {
			val metaGraph = event.newData!!.content as MetaGraph
			setData(
				metaGraph.graph.graphView,
				metaGraph.containerDrawing,
				editable = event.newData?.savable?.editable ?: false,
				isManualContainer = metaGraph.isManualContainer
			)
		} else {
			editable = false
		}
		updateEditability()
	}

	private fun handle(event: ApplicationDataContentEvent) {
		if (event.data.content is MetaGraph) {
			val metaGraph = event.data.content as MetaGraph
			setData(
				metaGraph.graph.graphView,
				metaGraph.containerDrawing,
				editable,
				isManualContainer = metaGraph.isManualContainer,
				applyZoomStrategy = false
			)
		}
	}

	private fun handle(event: CommandEvent) {
		if (editor.commandManager === event.commandManager) {
			isManualContainerCurrent = isManualContainer(isManualContainerOrig, editor.commandManager)
		}
	}

	private fun updateEditability() {
		editor.active = editable && containerDrawing != null
		drawingView.editable = editor.active
	}

	private fun notifyViewUpdate() {
		updateEditability()
		view.dataChanged()
	}

	private inner class GenerateContainerAction : AbstractAction("graph.action.containerLayout") {
		override fun execute(event: ActionEvent) {
			if (view.conformGenerateContainerDrawing()) {
				try {
					editor.commandManager.addTag(GraphFrameController.GENERATE_CONTAINER_TAG)
					editor.commandManager.execute(GenerateContainerCommand(this@ContainerPanelController))
					editor.view.applyDefaultZoomStrategy()
				} finally {
					editor.commandManager.removeTag(GraphFrameController.GENERATE_CONTAINER_TAG)
				}
			}
		}
	}

	private class GenerateContainerCommand(
		private val controller: ContainerPanelController
	) : AbstractCommand("graph.action.containerLayout.name") {

		override fun execute() {
			controller.generateContainerDrawing()
		}
	}
}