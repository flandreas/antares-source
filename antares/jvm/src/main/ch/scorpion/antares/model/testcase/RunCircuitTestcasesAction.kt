package ch.scorpion.antares.model.testcase

import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.antares.model.module.AntaresModelModule
import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.invocation.InvocationHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.model.ComponentMessage
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.app.ApplicationModeHolder
import ch.scorpion.jabbah.io.StorableCloner

/**
 * Runs all [Testcase]s of the selected [DigitalGraph]
 */
class RunCircuitTestcasesAction(
	application: Application,
	applicationModeHolder: ApplicationModeHolder,
	service: TestcaseAppService = AntaresModelModule.testcaseAppService,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractTestcaseAction("antares.testcase.action.runAll", application, applicationModeHolder, service, eventBus) {

	private val circuit: DigitalGraph get() =
		(application.controller.data!!.content as MetaGraph).graph.graphView.graph as DigitalGraph

	override fun calculateEnabled(): Boolean = super.calculateEnabled() && testcase == null

	override fun execute(event: ActionEvent) {
		InvocationHandler.invoke {
			val clone = StorableCloner.clone(circuit)
			val results = mutableListOf<CombinedTestRunResult>()
			for (testcase in circuit.testcases.testcases) {
				val runner = CombinedTestcaseRunner(testcase, clone)
				results.add(runner.run())
			}
			if (results.isEmpty()) {
				eventBus.post(ComponentMessage(source = null, messageKey = "antares.testcase.results.empty.text"))
			} else {
				eventBus.post(ComponentMessage(source = null, messageKey = "antares.testcase.results.done.text"))
			}
			eventBus.post(DisplayTestRunResults(results))
		}
	}
}