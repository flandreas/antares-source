package ch.scorpion.antares.view

import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.CurrentGraphAnimationTypeEvent
import ch.scorpion.jabbah.graph.view.CurrentGraphViewAnimationType
import ch.scorpion.jabbah.graph.view.GraphViewAnimationType

/**
 * An [Action] for activating or deactivating [GraphView] animation by changing [CurrentGraphViewAnimationType].
 */
class GraphViewAnimationAction(
        private val currentGraphViewAnimationType: CurrentGraphViewAnimationType,
        eventBus: EventBus = BaseModule.eventBus
) : AbstractAction("simulator.action.simulationDriver.animation") {

    init {
        eventBus.register(CurrentGraphAnimationTypeEvent::class, { updateState() })
        updateState()
    }

    private fun updateState() {
        selected =currentGraphViewAnimationType.graphViewAnimationType == GraphViewAnimationType.Animation
    }

    override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
        currentGraphViewAnimationType.graphViewAnimationType = if (selected)
            GraphViewAnimationType.Animation else GraphViewAnimationType.None
    }

}