package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.draw.view.ViewManager
import ch.scorpion.jabbah.edit.app.AbstractSelectionAwareAction
import ch.scorpion.jabbah.graph.view.vertice.OpenSubGraphRequest
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView

/**
 * Opens the currently selected [SubGraphVerticeView] in a new [GraphNavigationView]
 * in the [GraphDesktopView].
 * @param subGraphVerticeView set during execution, when there is no selection
 */
class OpenGraphNavigationAction(
	viewManager: ViewManager = DrawViewModule.viewManager,
	eventBus: EventBus = BaseModule.eventBus,
	var subGraphVerticeView: SubGraphVerticeView<*>? = null
) : AbstractSelectionAwareAction("graph.action.openSubGraph", eventBus, viewManager) {

	override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
		eventBus.post(OpenSubGraphRequest(subGraphVerticeView
			?: singleSelection as SubGraphVerticeView<*>, newView = true, quickMode = false))
	}

	override fun calculateEnabled(): Boolean {
		// Don't call super.calculateEnabled(), whose restrictions regarding editability are not needed
		return subGraphVerticeView != null || selectionCount == 1 && singleSelection is SubGraphVerticeView<*>
	}
}