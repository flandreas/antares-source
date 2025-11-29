package ch.scorpion.antares.model.testcase

import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.invocation.InvocationHandler
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.model.ComponentMessage
import ch.scorpion.jabbah.edit.model.ComponentMessageType
import ch.scorpion.jabbah.graph.MetaGraph

/**
 * Runs the currently selected [Testcase] and posts [DisplayTestRunResults] on the [EventBus].
 */
class RunTestcaseAction(
	controller: TestcaseViewController,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractTestcaseAction(controller, "antares.testcase.action.run", eventBus) {

	companion object {
		private val LOG by logger(RunTestcaseAction::class)
	}

	override val requestFocusOnClick: Boolean get() = true

	override fun execute(event: ActionEvent) {
		InvocationHandler.invoke {
			val metaGraph = controller.applicationDataHolder.data!!.content as MetaGraph
			val circuit = metaGraph.graph.model as DigitalGraph

			LOG.userTrail("Run testcase '${controller.testcase!!.name.value}' in circuit '${circuit.name.value}'")
			val results = TestcaseService.run(metaGraph, listOf(controller.testcase!!))

			val msgType = if (results.any { it.failed }) ComponentMessageType.Error else ComponentMessageType.Info
			eventBus.post(ComponentMessage(msgType, source = null, messageKey = "antares.testcase.results.done.text"))
			eventBus.post(DisplayTestRunResults(results))
		}
	}
}