package ch.scorpion.jabbah.draw.view

import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.PropertyChangeEvent
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.View

/**
 * A base implementation of an [Action] that acts on the currently active [View] in a [ContentViewManager]
 * and that disables itself if no [View] is active.
 *
 * Listens for [PropertyChangeEvent] from the active [View] and calls [handleViewPropertyChanged] to
 * allow subclasses to handle them.
 */
abstract class AbstractViewAction(
	baseName: String,
	eventBus: EventBus = BaseModule.eventBus,
	viewManager: ContentViewManager = DrawViewModule.viewManager
) : AbstractContentViewAction(baseName, eventBus, viewManager) {

	override fun calculateEnabled(): Boolean = calculateViewActionEnabled()

	protected fun calculateViewActionEnabled(): Boolean =
		viewManager.activeView != null && viewManager.activeView!!.view != null
}

