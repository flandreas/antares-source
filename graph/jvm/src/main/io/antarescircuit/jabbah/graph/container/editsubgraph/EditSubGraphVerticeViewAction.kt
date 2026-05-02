package io.antarescircuit.jabbah.graph.container.editsubgraph

import io.antarescircuit.jabbah.base.Action
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.swing.UiUtil
import io.antarescircuit.jabbah.draw.view.ContentViewManager
import io.antarescircuit.jabbah.draw.view.DrawViewModule
import io.antarescircuit.jabbah.edit.CommandManager
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.app.AbstractSelectionAwareAction
import io.antarescircuit.jabbah.edit.module.EditModule
import io.antarescircuit.jabbah.graph.GraphApplicationContextHolder
import io.antarescircuit.jabbah.graph.MetaGraphRepository
import io.antarescircuit.jabbah.graph.container.ContainerDrawing
import io.antarescircuit.jabbah.graph.container.ContainerEditor
import io.antarescircuit.jabbah.graph.container.ContainerPanelSwing
import io.antarescircuit.jabbah.graph.library.LibraryModule
import io.antarescircuit.jabbah.graph.ui.container.ContainerPanelController
import io.antarescircuit.jabbah.graph.view.GraphElementView
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeView

/**
 * An [Action] for editing the look of an individual [SubGraphVerticeView] by overwriting the standard
 * [ContainerDrawing] using a [ContainerEditor] in a dialog.
 */
class EditSubGraphVerticeViewAction(
	private val applicationContextHolder: GraphApplicationContextHolder,
	eventBus: EventBus = BaseModule.eventBus,
	viewManager: ContentViewManager = DrawViewModule.viewManager,
	private val commandManager: CommandManager = EditModule.commandManager,
	private val metaGraphRepository: MetaGraphRepository = LibraryModule.libraryHolder
) : AbstractSelectionAwareAction("graph.action.editSubGraphVerticeView", eventBus, viewManager) {

	companion object {
		private val LOG by logger(EditSubGraphVerticeViewAction::class)
	}

	override val opensDialog: Boolean get() = true

	override fun calculateEnabled(): Boolean {
		return super.calculateEnabled() && selectionCount == 1 && singleSelection is SubGraphVerticeView<*>
	}

	override fun execute(event: ActionEvent) {
		val editedVerticeView = singleSelection as SubGraphVerticeView<*>
		val editedDrawingView = drawingView

		LOG.userTrail("Edit symbol of SubGraphVerticeView ${editedVerticeView.id} with MetaGraph '${editedVerticeView.subGraphVertice?.name}' ${editedVerticeView.model.graphUUID}")

		editedDrawingView!!.selectionManager.deselect(editedVerticeView)
		editedVerticeView.invalidate()

		val containerPanelController = ContainerPanelController(
			applicationContextHolder,
			displayGlobalMessages = false,
			viewManager.castedActiveView<DrawingView<GraphElementView<*>, GraphView>>()!!
		)
		containerPanelController.editor.preventDeletingPortViewComponents = true

		val containerPanel = ContainerPanelSwing(
			containerPanelController,
			application = null,
			eventBus = eventBus,
			viewManager = viewManager)

		val oldActiveView = viewManager.activeView

		containerPanel.initialize()

		UiUtil.invokeLater {
			containerPanelController.drawingView.navigator.fitMaxNormal()
			containerPanelController.active = true
		}

		val editedContainerDrawing = EditSubGraphVerticeViewPanel.showAsDialog(
			title = name,
			metaGraphRepository = metaGraphRepository,
			containerPanel = containerPanel,
			subGraphVerticeView = editedVerticeView,
			commandManager = commandManager
		)

		editedContainerDrawing?.let {
			LOG.userTrail("Commit changes of symbol")
			commandManager.execute(EditSubGraphVerticeViewCommand(editedDrawingView, editedVerticeView.id, it))
		} ?: LOG.userTrail("Cancelled editing symbol")

		viewManager.activeView = oldActiveView

		editedVerticeView.invalidate()
		editedDrawingView.selectionManager.select(editedVerticeView)

		containerPanelController.dispose()
	}
}
