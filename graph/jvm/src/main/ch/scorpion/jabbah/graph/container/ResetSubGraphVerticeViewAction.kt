package ch.scorpion.jabbah.graph.container

import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.draw.view.ViewManager
import ch.scorpion.jabbah.edit.CommandManager
import ch.scorpion.jabbah.edit.app.AbstractSelectionAwareAction
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.graph.library.Library
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import javax.swing.Action

/**
 * An [Action] for resetting the custom look of a [SubGraphVerticeView] to the default look as defined
 * by the [ContainerDrawing] in the [Library].
 */
class ResetSubGraphVerticeViewAction(
	eventBus: EventBus = BaseModule.eventBus,
	viewManager: ViewManager = DrawViewModule.viewManager,
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
		LOG.trace("Resetting custom view of SubGraphVerticeView")
		commandManager.execute(EditSubGraphVerticeViewCommand(drawingView!!, subGraphVerticeView.id, null))
	}
}