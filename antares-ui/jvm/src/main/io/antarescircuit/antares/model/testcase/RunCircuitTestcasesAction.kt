package io.antarescircuit.antares.model.testcase

import io.antarescircuit.antares.model.DigitalGraph
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.invocation.InvocationHandler
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.edit.model.ComponentMessage
import io.antarescircuit.jabbah.edit.model.ComponentMessageType
import io.antarescircuit.jabbah.graph.MetaGraph

/**
 * Runs all [Testcase]s of the selected [DigitalGraph] and posts [DisplayTestRunResults] on the [EventBus].
 */
class RunCircuitTestcasesAction(
	controller: TestcaseViewController,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractTestcaseAction(controller, "antares.testcase.action.runAll", eventBus) {

	companion object {
		private val LOG by logger(RunCircuitTestcasesAction::class)
	}

	override fun calculateEnabled(): Boolean = super.calculateEnabled() && controller.testcase == null

	override fun execute(event: ActionEvent) {
		InvocationHandler.invoke {
			val metaGraph = controller.applicationDataHolder.data!!.content as MetaGraph
			val circuit = metaGraph.graph.model as DigitalGraph

			LOG.userTrail("Run all tests in circuit '${circuit.name.value}'")
			val results = TestcaseService.run(metaGraph, circuit.testcases.testcases)

			if (results.isEmpty()) {
				eventBus.post(ComponentMessage(source = null, messageKey = "antares.testcase.results.empty.text"))
			} else {
				val msgType = if (results.any { it.failed }) ComponentMessageType.Error else ComponentMessageType.Info
				eventBus.post(ComponentMessage(msgType, source = null, messageKey = "antares.testcase.results.done.text"))
			}
			eventBus.post(DisplayTestRunResults(results))
		}
	}
}