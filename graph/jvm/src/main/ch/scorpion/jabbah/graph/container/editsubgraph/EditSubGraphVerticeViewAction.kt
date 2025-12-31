package ch.scorpion.jabbah.graph.container.editsubgraph

import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.swing.UiUtil
import ch.scorpion.jabbah.draw.view.ContentViewManager
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.edit.CommandManager
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.app.AbstractSelectionAwareAction
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.graph.GraphApplicationContextHolder
import ch.scorpion.jabbah.graph.MetaGraphRepository
import ch.scorpion.jabbah.graph.container.ContainerDrawing
import ch.scorpion.jabbah.graph.container.ContainerEditor
import ch.scorpion.jabbah.graph.container.ContainerPanelSwing
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.ui.container.ContainerPanelController
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView

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
			viewManager.activeView!!.view as DrawingView<Drawing<Component>>,
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
