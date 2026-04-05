package io.antarescircuit.antares.view

import io.antarescircuit.jabbah.base.Action
import io.antarescircuit.jabbah.base.AbstractAction
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.event.EventHandler
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.CurrentGraphAnimationTypeEvent
import io.antarescircuit.jabbah.graph.view.CurrentGraphViewAnimationType
import io.antarescircuit.jabbah.graph.view.GraphViewAnimationType

/**
 * An [Action] for activating or deactivating [GraphView] animation by changing [CurrentGraphViewAnimationType].
 */
class GraphViewAnimationAction(
	private val currentGraphViewAnimationType: CurrentGraphViewAnimationType,
	private val eventBus: EventBus = BaseModule.eventBus
) : AbstractAction("execution.action.simulationDriver.animation") {

	private val currentGraphAnimationTypeHandler: EventHandler<CurrentGraphAnimationTypeEvent> = { updateState() }

	init {
		eventBus.register(CurrentGraphAnimationTypeEvent::class, currentGraphAnimationTypeHandler)
		updateState()
	}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(currentGraphAnimationTypeHandler)
	}

	private fun updateState() {
		selected = currentGraphViewAnimationType.graphViewAnimationType == GraphViewAnimationType.Animation
	}

	override fun execute(event: io.antarescircuit.jabbah.base.event.ActionEvent) {
		currentGraphViewAnimationType.graphViewAnimationType = if (selected)
			GraphViewAnimationType.Animation else GraphViewAnimationType.None
	}
}