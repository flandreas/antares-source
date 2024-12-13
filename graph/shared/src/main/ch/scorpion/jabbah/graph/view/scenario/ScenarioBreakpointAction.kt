package ch.scorpion.jabbah.graph.view.scenario

import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule

/** An [Action] for toggling [ScenarioBreakpoints]. */
class ScenarioBreakpointAction(
    private val scenarioBreakpoints: ScenarioBreakpoints,
    private val eventBus: EventBus = BaseModule.eventBus
) : AbstractAction("graph.action.scenario.breakpoints") {

    private val scenarioBreakpointsHandler: EventHandler<ScenarioBreakpointEnablingEvent> = {
        if (it.source === scenarioBreakpoints) {
            update()
        }
    }

    init {
        eventBus.register(ScenarioBreakpointEnablingEvent::class, scenarioBreakpointsHandler)
        update()
    }

    private fun update() {
        selected = scenarioBreakpoints.enabled
        enabled = true
    }

    override fun dispose() {
        super.dispose()
        eventBus.unregister(scenarioBreakpointsHandler)
    }

    override fun execute(event: ActionEvent) {
        scenarioBreakpoints.enabled = !scenarioBreakpoints.enabled
    }
}