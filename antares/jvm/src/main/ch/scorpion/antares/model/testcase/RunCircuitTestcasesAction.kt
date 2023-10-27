package ch.scorpion.antares.model.testcase

import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.antares.model.module.AntaresModelModule
import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.invocation.InvocationHandler
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.model.ComponentMessage
import ch.scorpion.jabbah.edit.model.ComponentMessageType
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.app.ApplicationModeHolder

/**
 * Runs all [Testcase]s of the selected [DigitalGraph] and posts [DisplayTestRunResults] on the [EventBus].
 */
class RunCircuitTestcasesAction(
	application: Application,
	applicationModeHolder: ApplicationModeHolder,
	service: TestcaseAppService = AntaresModelModule.testcaseAppService,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractTestcaseAction("antares.testcase.action.runAll", application, applicationModeHolder, service, eventBus) {

	companion object {
		private val LOG by logger(RunCircuitTestcasesAction::class)
	}

	override fun calculateEnabled(): Boolean = super.calculateEnabled() && testcase == null

	override fun execute(event: ActionEvent) {
		InvocationHandler.invoke {
			val metaGraph = application.controller.data!!.content as MetaGraph
			val circuit = metaGraph.graph.model as DigitalGraph

			LOG.userTrail("Run all tests in circuit '${circuit.name.value}'")
			val results = RunTestcaseAction.run(metaGraph, circuit.testcases.testcases)

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