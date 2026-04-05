package io.antarescircuit.jabbah.draw.view.find

import io.antarescircuit.jabbah.base.AbstractAction
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.event.EventHandler
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.draw.view.ContentView
import io.antarescircuit.jabbah.draw.view.ActiveContentViewChangedEvent
import io.antarescircuit.jabbah.draw.view.ContentViewManager
import io.antarescircuit.jabbah.draw.view.DrawViewModule

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
			updateEnabled()
		}
	}

	init {
		eventBus.register(ActiveContentViewChangedEvent::class, contentViewHandler)
		updateEnabled()
	}

	override fun dispose() {
		eventBus.unregister(contentViewHandler)
	}

	override fun execute(event: ActionEvent) {
		viewManager.activeView?.showSearchBar()
	}

	override fun calculateEnabled(): Boolean = viewManager.activeView?.canSearch == true
}