package ch.scorpion.jabbah.graph.container

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.swing.UiUtil
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.draw.view.ContentViewManager
import ch.scorpion.jabbah.edit.CommandManager
import ch.scorpion.jabbah.edit.app.AbstractSelectionAwareAction
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.edit.module.EditModuleJvm
import ch.scorpion.jabbah.graph.GraphApplicationContextHolder
import ch.scorpion.jabbah.graph.MetaGraphRepository
import ch.scorpion.jabbah.graph.module.GraphModule
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import javax.swing.Action

/**
 * An [Action] for editing the look of a individual [SubGraphVerticeView] by overwriting the standard
 * [ContainerDrawing] using a [ContainerEditor] in a dialog.
 */
class EditSubGraphVerticeViewAction(
	private val applicationContextHolder: GraphApplicationContextHolder,
	eventBus: EventBus = BaseModule.eventBus,
	viewManager: ContentViewManager = DrawViewModule.viewManager,
	private val commandManager: CommandManager = EditModule.commandManager,
	private val metaGraphRepository: MetaGraphRepository = GraphModule.metaGraphRepository
) : AbstractSelectionAwareAction("graph.action.editSubGraphVerticeView", eventBus, viewManager) {

	companion object {
		private val LOG by logger(EditSubGraphVerticeViewAction::class)
	}

	override fun calculateEnabled(): Boolean {
		return super.calculateEnabled() && selectionCount == 1 && singleSelection is SubGraphVerticeView<*>
	}

	override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
		LOG.debug("opening EditSubGraphVerticeViewPanel")

		val editedVerticeView = singleSelection as SubGraphVerticeView<*>
		val editedDrawingView = drawingView

		editedDrawingView!!.selectionManager.deselect(editedVerticeView)
		editedVerticeView.invalidate()

		val containerDrawingView = EditModule.drawingViewFactory.create(ContainerDrawing(), applicationContextHolder, displayGlobalMessages = false)
		val containerPanel = ContainerPanelSwing(
			application = null,
			GraphViewModule.containerEditorFactory.invoke(containerDrawingView),
			EditModuleJvm.propertySheetPanelFactory,
			eventBus,
			viewManager)

		val oldActiveView = viewManager.activeView

		containerPanel.initialize()

		UiUtil.invokeLater(Runnable {
			containerPanel.editor.view.navigator.fitMaxNormal()
			containerPanel.active = true
		})

		if (EditSubGraphVerticeViewPanel.showAsDialog(
				metaGraphRepository = metaGraphRepository,
				containerPanel = containerPanel,
				subGraphVerticeView = editedVerticeView,
				commandManager = commandManager
			)) {
			// User has pressed "OK"
			commandManager.execute(EditSubGraphVerticeViewCommand(editedDrawingView, editedVerticeView.id, containerPanel.editor.drawing as ContainerDrawing))
		}

		viewManager.activeView = oldActiveView

		editedVerticeView.invalidate()
		editedDrawingView.selectionManager.select(editedVerticeView)

		containerPanel.dispose()
	}
}
