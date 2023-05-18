package ch.scorpion.jabbah.graph.ui.library

import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.view.ContentView
import ch.scorpion.jabbah.draw.view.AbstractContentViewAction
import ch.scorpion.jabbah.draw.view.ContentViewManager
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.ui.GraphNavigationView

/**
 * Locates the currently active [ContentView] containing a [MetaGraph]
 * in a [LibraryTreeView] by expanding the entire tree to the active element.
 */
class LocateActiveMetaGraphAction(
	private val controller: LibraryTreeViewController,
	viewManager: ContentViewManager = DrawViewModule.viewManager,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractContentViewAction("graph.action.locateActiveMetaGraph", eventBus, viewManager) {

	init {
		imagePath = "/img/crosshair.png"
		updateEnabled()
	}

	override fun calculateEnabled(): Boolean = super.calculateEnabled() && contentView is GraphNavigationView

	override fun execute(event: ActionEvent) {
		(contentView as GraphNavigationView).graphView.graph?.uuid?.let {
			controller.expandTo(it)
		}
	}
}