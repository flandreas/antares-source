package io.antarescircuit.jabbah.graph.container

import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.draw.view.ContentViewManager
import io.antarescircuit.jabbah.draw.view.DrawViewModule
import io.antarescircuit.jabbah.edit.CommandManager
import io.antarescircuit.jabbah.edit.app.AbstractSelectionAwareAction
import io.antarescircuit.jabbah.edit.module.EditModule
import io.antarescircuit.jabbah.graph.container.editsubgraph.EditSubGraphVerticeViewCommand
import io.antarescircuit.jabbah.graph.library.Library
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeView
import javax.swing.Action

/**
 * An [Action] for resetting the custom look of a [SubGraphVerticeView] to the default look as defined
 * by the [ContainerDrawing] in the [Library].
 */
class ResetSubGraphVerticeViewAction(
	eventBus: EventBus = BaseModule.eventBus,
	viewManager: ContentViewManager = DrawViewModule.viewManager,
	private val commandManager: CommandManager = EditModule.commandManager
) : AbstractSelectionAwareAction("graph.action.resetCustomView", eventBus, viewManager) {

	companion object {
		private val LOG by logger(ResetSubGraphVerticeViewAction::class)
	}

	private val subGraphVerticeView: SubGraphVerticeView<*> get() = singleSelection as SubGraphVerticeView<*>

	override fun calculateEnabled(): Boolean {
		return super.calculateEnabled() && singleSelection is SubGraphVerticeView<*> && subGraphVerticeView.hasCustomizedContainerDrawing
	}

	override fun execute(event: ActionEvent) {
		LOG.userTrail("Resetting custom view of SubGraphVerticeView")
		val vv = subGraphVerticeView
		// Deselect/select to get rid of the invalid inner SelectionModels
		drawingView!!.selectionManager.deselect(vv)
		commandManager.execute(EditSubGraphVerticeViewCommand(drawingView!!, vv.id, null))
		drawingView!!.selectionManager.select(vv)
	}
}