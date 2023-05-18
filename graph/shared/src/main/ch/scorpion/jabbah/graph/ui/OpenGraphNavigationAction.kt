package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.view.ContentViewManager
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.edit.app.AbstractSelectionAwareAction
import ch.scorpion.jabbah.graph.ui.desktop.GraphDesktopView
import ch.scorpion.jabbah.graph.view.vertice.OpenSubGraphRequest
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView

/**
 * Opens the currently selected [SubGraphVerticeView] in a new [GraphNavigationView]
 * in the [GraphDesktopView].
 * @param subGraphVerticeView set during execution, when there is no selection
 */
class OpenGraphNavigationAction(
	viewManager: ContentViewManager = DrawViewModule.viewManager,
	eventBus: EventBus = BaseModule.eventBus,
	var subGraphVerticeView: SubGraphVerticeView<*>? = null
) : AbstractSelectionAwareAction("graph.action.openSubGraph", eventBus, viewManager) {

	init {
		updateEnabled()
	}

	override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
		val vv = subGraphVerticeView ?: singleSelection as SubGraphVerticeView<*>
		eventBus.post(OpenSubGraphRequest(vv, newView = true, quickMode = false))
	}

	override fun calculateEnabled(): Boolean {
		// Don't call super.calculateEnabled(), whose restrictions regarding editability are not needed
		return subGraphVerticeView != null || selectionCount == 1 && singleSelection is SubGraphVerticeView<*>
	}
}