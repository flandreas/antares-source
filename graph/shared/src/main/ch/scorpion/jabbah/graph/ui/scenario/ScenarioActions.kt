package ch.scorpion.jabbah.graph.ui.scenario

import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.ui.AbstractApplicationDataEditModeAction
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.Scenario
import ch.scorpion.jabbah.graph.view.ScenarioStep

abstract class AbstractScenarioAction(
    protected val controller: ScenarioViewController,
    baseName: String,
    eventBus: EventBus = BaseModule.eventBus
) : AbstractApplicationDataEditModeAction(baseName, controller.applicationDataHolder, controller.applicationModeHolder, eventBus)


/**
 * Asks the user for the name of a new [Scenario] and adds it to the current [GraphView].
 */
class AddScenarioAction(
    controller: ScenarioViewController,
    eventBus: EventBus = BaseModule.eventBus
) : AbstractScenarioAction(controller, "scenarios.action.addScenario", eventBus) {

    override val opensDialog: Boolean get() = true

    override fun execute(event: ActionEvent) {
        controller.view.getNewScenarioName()?.let {
            controller.addScenario(it)
        }
    }

    override fun calculateEnabled(): Boolean =
        super.calculateEnabled()
            && applicationDataHolder.data?.content is MetaGraph
            && controller.scenario == null
            && controller.scenarioStep == null
}

/**
 * Asks the user for the name of a new [ScenarioStep] and adds it to the current [Scenario].
 */
class AddScenarioStepAction(
    controller: ScenarioViewController,
    eventBus: EventBus = BaseModule.eventBus
) : AbstractScenarioAction(controller,"scenarios.action.addScenarioStep", eventBus) {

    override val opensDialog: Boolean get() = true

    override fun execute(event: ActionEvent) {
        controller.view.getNewScenarioStepName()?.let {
            controller.addScenarioStep(it)
        }
    }

    override fun calculateEnabled(): Boolean =
        super.calculateEnabled()
            && controller.scenario != null
            && controller.scenarioStep == null
}

/** Deletes the currently selected [Scenario]. */
class DeleteScenarioAction(
    controller: ScenarioViewController,
    eventBus: EventBus = BaseModule.eventBus
) : AbstractScenarioAction(controller, "scenarios.action.deleteScenario", eventBus) {

    override val opensDialog: Boolean get() = true

    override fun execute(event: ActionEvent) {
        if (controller.view.confirmDeleteScenario()) {
            controller.deleteScenario()
        }
    }

    override fun calculateEnabled(): Boolean =
        super.calculateEnabled() && controller.scenario != null && controller.scenarioStep == null
}

class DeleteScenarioStepAction(
    controller: ScenarioViewController,
    eventBus: EventBus = BaseModule.eventBus
) : AbstractScenarioAction(controller, "scenarios.action.deleteScenarioStep", eventBus) {

    override val opensDialog: Boolean get() = true

    override fun execute(event: ActionEvent) {
        if (controller.view.confirmDeleteScenarioStep()) {
            controller.deleteScenarioStep()
        }
    }

    override fun calculateEnabled(): Boolean =
        super.calculateEnabled() && controller.scenario != null && controller.scenarioStep != null
}