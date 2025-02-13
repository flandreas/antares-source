package ch.scorpion.jabbah.draw.view.find

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.view.ContentView
import ch.scorpion.jabbah.draw.view.ActiveContentViewChangedEvent
import ch.scorpion.jabbah.draw.view.ContentViewManager
import ch.scorpion.jabbah.draw.view.DrawViewModule

/**
 * Displays a UI of [Searchable] for the currently active [ContentView], if it supports
 * search functionality.
 */
class FindAction(
	private val eventBus: EventBus = BaseModule.eventBus,
	private val viewManager: ContentViewManager = DrawViewModule.viewManager
) : AbstractAction("draw.action.find", opensDialog = true) {

	private val contentViewHandler: EventHandler<ActiveContentViewChangedEvent> = {
		if (it.viewManager === viewManager) {
			updateEnabledness()
		}
	}

	init {
		eventBus.register(ActiveContentViewChangedEvent::class, contentViewHandler)
		updateEnabledness()
	}

	override fun dispose() {
		eventBus.unregister(contentViewHandler)
	}

	override fun execute(event: ActionEvent) {
		viewManager.activeView?.showSearchBar()
	}

	private fun updateEnabledness() {
		enabled = viewManager.activeView?.canSearch == true
	}
}