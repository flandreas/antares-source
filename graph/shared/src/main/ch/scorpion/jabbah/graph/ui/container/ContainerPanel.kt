package ch.scorpion.jabbah.graph.ui.container

import ch.scorpion.jabbah.app.ApplicationDataContentEvent
import ch.scorpion.jabbah.app.ApplicationDataEvent
import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.ui.AbstractUIController
import ch.scorpion.jabbah.base.ui.UIView
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.command.AbstractCommand
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.edit.properties.ComponentPropertyPanelController
import ch.scorpion.jabbah.graph.GraphApplicationContextHolder
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.container.ContainerDrawing
import ch.scorpion.jabbah.graph.container.isManualContainer
import ch.scorpion.jabbah.graph.ui.GraphFrameController
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView

/**
 * A [UIView] for editing the outside [ContainerDrawing] of a [GraphView].
 */
interface ContainerPanelView : UIView {

	fun dataChanged()

	fun updateIsManualContainer(isManualContainer: Boolean)

	fun conformGenerateContainerDrawing(): Boolean

	fun generateContainerDrawing()

	fun activeChanged()
}

class ContainerPanelController(
	applicationContextHolder: GraphApplicationContextHolder,
	displayGlobalMessages: Boolean = true,
	val mainGraphDrawingView: DrawingView<Drawing<Component>>,
	private val eventBus: EventBus = BaseModule.eventBus
) : AbstractUIController<ContainerPanelView>() {

	val drawingView = EditModule.drawingViewFactory.create(ContainerDrawing(), applicationContextHolder, displayGlobalMessages,
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
		private set(value) {
			field = value
			view.updateIsManualContainer(field)
		}

	var active: Boolean = false
		set(value) {
			if (field != value) {
				field = value
				// Update the UI of the JTree in order to recalculate the width of the TreeRenderer's JLabels,
				// which are obviously cached by the JTree's UIManager. The tree nodes display the names of domain object,
				// and these names might have been changed while the ContainerPanel wasn't active
				view.activeChanged()
				editor.active = value && editable
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

	private fun handle(event: ApplicationDataEvent) {
		if (event.newData == null) {
			graphView = null
			containerDrawing = null
			editable = false
			notifyViewUpdate()

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
			containerDrawing = metaGraph.containerDrawing
			setData(
				metaGraph.graph.graphView,
				containerDrawing!!,
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