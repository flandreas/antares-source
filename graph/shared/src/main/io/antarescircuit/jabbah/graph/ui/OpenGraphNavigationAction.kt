package io.antarescircuit.jabbah.graph.ui

import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.event.EventHandler
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.draw.view.ContentViewManager
import io.antarescircuit.jabbah.draw.view.DrawViewModule
import io.antarescircuit.jabbah.edit.app.AbstractSelectionAwareAction
import io.antarescircuit.jabbah.execution.scheduler.SchedulerActivationStateEvent
import io.antarescircuit.jabbah.graph.ui.desktop.GraphDesktopView
import io.antarescircuit.jabbah.graph.view.vertice.OpenSubGraphRequest
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeView

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

	private val schedulerActivationStateHandler: EventHandler<SchedulerActivationStateEvent> = {
		subGraphVerticeView = null
		updateEnabled()
	}

	init {
		eventBus.register(SchedulerActivationStateEvent::class, schedulerActivationStateHandler)
		updateEnabled()
	}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(schedulerActivationStateHandler)
	}

	override fun execute(event: io.antarescircuit.jabbah.base.event.ActionEvent) {
		val vv = subGraphVerticeView ?: singleSelection as SubGraphVerticeView<*>
		eventBus.post(OpenSubGraphRequest(vv, newView = true, quickMode = false))
	}

	override fun calculateEnabled(): Boolean {
		// Don't call super.calculateEnabled(), whose restrictions regarding editability are not needed
		return subGraphVerticeView != null || selectionCount == 1 && singleSelection is SubGraphVerticeView<*>
	}
}