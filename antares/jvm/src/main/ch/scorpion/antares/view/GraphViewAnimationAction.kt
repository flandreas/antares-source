package ch.scorpion.antares.view

import ch.scorpion.antares.view.module.AntaresViewModule
import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.view.CurrentGraphAnimationTypeEvent
import ch.scorpion.jabbah.graph.view.CurrentGraphViewAnimationType
import ch.scorpion.jabbah.graph.view.GraphViewAnimationType
import java.awt.event.ActionEvent
import javax.swing.Action

/**
 * An [Action] for activating or deactivating [GraphView] animation by changing [CurrentGraphViewAnimationType].
 */
class GraphViewAnimationAction(
        private val currentGraphViewAnimationType: CurrentGraphViewAnimationType,
        eventBus: EventBus
) : AbstractAction("simulator.action.simulationDriver.animation") {
    constructor(): this(AntaresViewModule.currentGraphViewAnimationType, BaseModule.eventBus)

    init {
        eventBus.register(CurrentGraphAnimationTypeEvent::class, { updateState() })
        updateState()
    }

    private fun updateState() {
        putValue(Action.SELECTED_KEY, currentGraphViewAnimationType.graphViewAnimationType == GraphViewAnimationType.Animation)
    }

    override fun actionPerformed(e: ActionEvent?) {
        currentGraphViewAnimationType.graphViewAnimationType = if (getValue(Action.SELECTED_KEY) as Boolean)
            GraphViewAnimationType.Animation else GraphViewAnimationType.None
    }
}