package io.antarescircuit.jabbah.edit.app

import io.antarescircuit.jabbah.base.Action
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.draw.view.ContentViewManager
import io.antarescircuit.jabbah.draw.view.DrawViewModule
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.Drawing
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.model.group.GroupComponent
import io.antarescircuit.jabbah.edit.module.EditModule

/** An [Action] for grouping the selected [Component]s to a [GroupComponent].*/
class GroupComponentsAction(
	eventBus: EventBus = BaseModule.eventBus,
	viewManager: ContentViewManager = DrawViewModule.viewManager,
	private val service: DrawingAppService = EditModule.drawingAppService
) : AbstractSelectionAwareAction("edit.action.group", eventBus, viewManager) {

	override fun execute(event: ActionEvent) {
		val drawingView = viewManager.castedActiveView<DrawingView<Component, Drawing<Component>>>()!!
		service.group(
			drawingView.selectionManager.selection.toCollection(mutableListOf()),
			drawingView)
	}

	override fun calculateEnabled(): Boolean =
		super.calculateEnabled() && selectionCount >= 2
}

/** An [Action] for un-grouping the selected [GroupComponent].*/
class UngroupComponentsAction(
	eventBus: EventBus = BaseModule.eventBus,
	viewManager: ContentViewManager = DrawViewModule.viewManager,
	private val service: DrawingAppService = EditModule.drawingAppService
) : AbstractSelectionAwareAction("edit.action.ungroup", eventBus, viewManager) {

	override fun execute(event: ActionEvent) {
		val drawingView = viewManager.castedActiveView<DrawingView<Component, Drawing<Component>>>()!!
		service.ungroup(
			singleSelection!!.propertyOwner as GroupComponent,
			drawingView)
	}

	override fun calculateEnabled(): Boolean =
		super.calculateEnabled() && selectionCount == 1 && singleSelection!!.propertyOwner is GroupComponent
}
