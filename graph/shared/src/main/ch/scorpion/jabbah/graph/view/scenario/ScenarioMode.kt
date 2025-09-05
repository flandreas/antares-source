package ch.scorpion.jabbah.graph.view.scenario

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.EnumProperty
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import ch.scorpion.jabbah.execution.speed.SystemSpeedCategory
import ch.scorpion.jabbah.graph.view.scenario.ScenarioMode.SpeedLimitOrSlower

/**
 * Determines if and when scenario text are displayed during simulation.
 */
enum class ScenarioMode(
    override val customName: String,
) : EnumProperty<ScenarioMode> {
    Never("never") {
        override fun toString(): String = Translations.getString("scenario.mode.never")
    },

    SpeedLimitOrSlower("speedLimitOrSlower") {
        override fun toString(): String =
            Translations.getString("scenario.mode.speedLimitOrSlower", CurrentScenarioMode.speedLimit.toString())
    };

    companion object {

        const val PROP_SCENARIO_MODE = "graph.scenario.mode"

        fun withName(customName: String): ScenarioMode =
            ScenarioMode.entries.firstOrNull { it.customName == customName }
                ?: throw IllegalArgumentException("unknown ScenarioMode '$customName'")
    }
}

object CurrentScenarioMode {

    val speedLimit: SystemSpeedCategory by lazy {
        SystemSpeedCategory.withName(BaseModule.properties.getString(Scheduler.PROP_SCHEDULER_EVENT_SYSTEM_SPEED_LIMIT))
    }

    var mode: ScenarioMode = fromProperties
        set(value) {
            if (value != field) {
                field = value
                BaseModule.properties.customize(ScenarioMode.PROP_SCENARIO_MODE, field.customName)
                BaseModule.eventBus.post(CurrentScenarioModeEvent(field))
            }
        }

    fun displayTextForSpeedCategory(speedCategory: SystemSpeedCategory): Boolean =
        mode === SpeedLimitOrSlower && speedCategory >= speedLimit

    private val fromProperties: ScenarioMode get() =
        ScenarioMode.withName(BaseModule.properties.getString(ScenarioMode.PROP_SCENARIO_MODE))
}

data class CurrentScenarioModeEvent(val mode: ScenarioMode)

class ScenarioModeAction(
    private val mode: ScenarioMode,
    private val eventBus: EventBus = BaseModule.eventBus
) : AbstractAction("scenario.mode") {

    private val currentScenarioModeHandler: EventHandler<CurrentScenarioModeEvent> = { updateState() }

    override var name: String
        get() = mode.toString()
        set(_) {}

    init {
        eventBus.register(CurrentScenarioModeEvent::class, currentScenarioModeHandler)
        updateState()
    }

    override fun dispose() {
        super.dispose()
        eventBus.unregister(currentScenarioModeHandler)
    }

    override fun execute(event: ActionEvent) {
        CurrentScenarioMode.mode = mode
    }

    private fun updateState() {
        selected = CurrentScenarioMode.mode === mode
    }
}