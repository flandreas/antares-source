package ch.scorpion.jabbah.edit.app

import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.draw.view.ViewManager
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.model.group.GroupComponent
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.module.EditModule

/** An [Action] for grouping the selected [Component]s to a [GroupComponent].*/
class GroupComponentsAction(
	eventBus: EventBus = BaseModule.eventBus,
	viewManager: ViewManager = DrawViewModule.viewManager,
	private val service: DrawingAppService = EditModule.drawingAppService
) : AbstractSelectionAwareAction("edit.action.group", eventBus, viewManager) {

	override fun execute(event: ActionEvent) {
		val drawingView = viewManager.castedActiveView<DrawingView<Drawing<Component>>>()!!
		service.group(
			drawingView.selectionManager.selection.toCollection(mutableListOf()),
			drawingView)
	}

	override fun calculateEnabled(): Boolean {
		return super.calculateEnabled() && selectionCount >= 2
	}
}

/** An [Action] for un-grouping the selected [GroupComponent].*/
class UngroupComponentsAction(
	eventBus: EventBus = BaseModule.eventBus,
	viewManager: ViewManager = DrawViewModule.viewManager,
	private val service: DrawingAppService = EditModule.drawingAppService
) : AbstractSelectionAwareAction("edit.action.ungroup", eventBus, viewManager) {

	override fun execute(event: ActionEvent) {
		val drawingView = viewManager.castedActiveView<DrawingView<Drawing<Component>>>()!!
		service.ungroup(
			singleSelection!!.propertyOwner as GroupComponent,
			drawingView)
	}

	override fun calculateEnabled(): Boolean {
		return super.calculateEnabled() && selectionCount == 1 && singleSelection!!.propertyOwner is GroupComponent
	}
}
