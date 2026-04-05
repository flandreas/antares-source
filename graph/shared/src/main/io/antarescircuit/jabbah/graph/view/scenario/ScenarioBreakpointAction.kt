package io.antarescircuit.jabbah.graph.view.scenario

import io.antarescircuit.jabbah.base.Action
import io.antarescircuit.jabbah.base.AbstractAction
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.event.EventHandler
import io.antarescircuit.jabbah.base.module.BaseModule

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