package ch.scorpion.jabbah.graph.view.scenario

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule

class ScenarioBreakpoints(
    private val eventBus: EventBus = BaseModule.eventBus
) {

    companion object {
        const val PROP_ENABLED = "jabbah.graph.ScenarioBreakpoints"
    }


    var enabled: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                BaseModule.properties.set(PROP_ENABLED, field)
                eventBus.post(ScenarioBreakpointEnablingEvent(this))
            }
        }

    init {
        enabled = BaseModule.properties.getBoolean(PROP_ENABLED)
    }
}

class ScenarioBreakpointEnablingEvent(val source: ScenarioBreakpoints)