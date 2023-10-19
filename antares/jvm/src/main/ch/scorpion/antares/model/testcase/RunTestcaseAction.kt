package ch.scorpion.antares.model.testcase

import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.antares.model.module.AntaresModelModule
import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.invocation.InvocationHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.model.ComponentMessage
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.app.ApplicationModeHolder
import ch.scorpion.jabbah.io.StorableCloner

/**
 * Runs the currently selected [Testcase] and posts [DisplayTestRunResults] on the [EventBus].
 */
class RunTestcaseAction(
	application: Application,
	applicationModeHolder: ApplicationModeHolder,
	service: TestcaseAppService = AntaresModelModule.testcaseAppService,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractTestcaseAction("antares.testcase.action.run", application, applicationModeHolder, service, eventBus) {

	private val circuit: DigitalGraph get() =
		(application.controller.data!!.content as MetaGraph).graph.graphView.graph as DigitalGraph

	override fun calculateEnabled(): Boolean =
		super.calculateEnabled() && StringUtils.isNotEmpty(testcase?.testVectors?.script)

	override fun execute(event: ActionEvent) {
		// Clone circuit to avoid interference from various objects of the main application,
		// such as GraphViewExecutionAnimator that listen on Actors of the main circuit
		val clone = StorableCloner.clone(circuit)

		val runner = CombinedTestcaseRunner(testcase!!, clone)

		InvocationHandler.invoke {
			val results = runner.run()
			eventBus.post(ComponentMessage(source = null, messageKey = "antares.testcase.results.done.text"))
			eventBus.post(DisplayTestRunResults(listOf(results)))
		}
	}
}