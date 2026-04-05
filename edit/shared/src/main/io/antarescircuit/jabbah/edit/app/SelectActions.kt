package io.antarescircuit.jabbah.edit.app

import io.antarescircuit.jabbah.base.Action
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.draw.view.ContentViewManager
import io.antarescircuit.jabbah.draw.view.DrawViewModule
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.Drawing


abstract class AbstractSelectionAction(
	baseName: String,
	eventBus: EventBus = BaseModule.eventBus,
	viewManager: ContentViewManager = DrawViewModule.viewManager
) : AbstractEditAction(baseName, eventBus, viewManager) {

	override fun calculateEnabled(): Boolean = calculateViewActionEnabled()
}

/** An [Action] for selecting all [Component]s in a [Drawing].*/
class SelectAllAction(
	eventBus: EventBus = BaseModule.eventBus,
	viewManager: ContentViewManager = DrawViewModule.viewManager
) : AbstractSelectionAction("edit.action.selectAll", eventBus, viewManager) {

	override fun execute(event: ActionEvent) {
		drawingView!!.selectionManager.selectAll()
		viewManager.activeView!!.view!!.repaint()
	}
}

/** An [Action] for selecting the next [Component] in a [Drawing].*/
class SelectNextAction(
	eventBus: EventBus = BaseModule.eventBus,
	viewManager: ContentViewManager = DrawViewModule.viewManager
) : AbstractSelectionAction("edit.action.selectNext", eventBus, viewManager) {

	override fun execute(event: ActionEvent) {
		drawingView!!.selectionManager.selectNext()
		viewManager.activeView!!.view!!.repaint()
	}
}

/** An [Action] for selecting the previous [Component] in a [Drawing].*/
class SelectPreviousAction(
	eventBus: EventBus = BaseModule.eventBus,
	viewManager: ContentViewManager = DrawViewModule.viewManager
) : AbstractSelectionAction("edit.action.selectPrevious", eventBus, viewManager) {

	override fun execute(event: ActionEvent) {
		drawingView!!.selectionManager.selectPrevious()
		viewManager.activeView!!.view!!.repaint()
	}
}