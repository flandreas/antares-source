package io.antarescircuit.jabbah.graph.view.scenario

import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.module.BaseModule

class ScenarioBreakpoints(
    private val eventBus: EventBus = BaseModule.eventBus
) {

    companion object {
        private const val SETTING_ENABLED = "jabbah.graph.ScenarioBreakpoints"
    }


    var enabled: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                BaseModule.settings.set(SETTING_ENABLED, field)
                eventBus.post(ScenarioBreakpointEnablingEvent(this))
            }
        }

    init {
        enabled = BaseModule.settings.getBoolean(SETTING_ENABLED, false)
    }
}

class ScenarioBreakpointEnablingEvent(val source: ScenarioBreakpoints)