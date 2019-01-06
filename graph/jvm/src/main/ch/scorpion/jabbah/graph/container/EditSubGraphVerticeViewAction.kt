package ch.scorpion.jabbah.graph.container

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.swing.UiUtil
import ch.scorpion.jabbah.draw.InputEventContext
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.draw.view.ViewManager
import ch.scorpion.jabbah.edit.app.AbstractSelectionAwareAction
import ch.scorpion.jabbah.edit.module.EditModuleJvm
import ch.scorpion.jabbah.graph.MetaGraphRepository
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import ch.scorpion.jabbah.io.IOModule
import ch.scorpion.jabbah.io.StorableCloner
import javax.swing.Action

/**
 * An [Action] for editing the look of a individual [SubGraphVerticeView] by overwriting the standard
 * [ContainerDrawing] using a [ContainerEditor] in a dialog.
 */
class EditSubGraphVerticeViewAction(
	private val eventBus: EventBus = BaseModule.eventBus,
	viewManager: ViewManager = DrawViewModule.viewManager,
	private val metaGraphRepository: MetaGraphRepository = GraphModelModule.metaGraphRepository,
	private val storableCloner: StorableCloner = IOModule.storableClonerProvider.invoke()
) : AbstractSelectionAwareAction("graph.action.editSubGraphVerticeView", eventBus, viewManager) {

	companion object {
		private val LOG by logger(EditSubGraphVerticeViewAction::class)
	}

	override fun calculateEnabled(): Boolean {
		return getSelectionCount() == 1 && getSingleSelection() is SubGraphVerticeView<*>
	}

	override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
		LOG.debug("opening EditSubGraphVerticeViewPanel")

		val editedVerticeView = getSingleSelection() as SubGraphVerticeView<*>
		getDrawingView()!!.selectionManager.deselect(editedVerticeView)
		editedVerticeView.invalidate()

		val containerPanel = ContainerPanel(
			GraphViewModule.containerEditorFactory.invoke(eventBus),
			EditModuleJvm.propertySheetPanelFactory,
			eventBus,
			viewManager)

		val oldActiveView = viewManager.activeView
		viewManager.registerView(containerPanel.editor.view)

		containerPanel.initialize()

		UiUtil.invokeLater(Runnable {
			containerPanel.editor.view.navigator.fitMaxNormal()
			containerPanel.activated()
		})

		if (EditSubGraphVerticeViewPanel.showAsDialog(
			metaGraphRepository = metaGraphRepository,
			containerPanel = containerPanel,
			subGraphVerticeView = editedVerticeView,
			storableCloner = storableCloner
		)) {
			// User has pressed "OK"
			editedVerticeView.setEditedContainerDrawing(containerPanel.editor.drawing as ContainerDrawing)
		}

		viewManager.unregisterView(containerPanel.editor.view)
		viewManager.activeView = oldActiveView

		editedVerticeView.invalidate()
		getDrawingView()!!.selectionManager.select(editedVerticeView)

		containerPanel.dispose()
	}
}