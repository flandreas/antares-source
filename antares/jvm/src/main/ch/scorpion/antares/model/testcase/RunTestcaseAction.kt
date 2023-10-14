package ch.scorpion.antares.model.testcase

import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.antares.model.module.AntaresModelModule
import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.invocation.InvocationHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.app.ApplicationModeHolder

/**
 * Runs the currently selected [Testcase] and displays [TestRunResultPanelSwing] with the results.
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
		InvocationHandler.invoke {
			try {
				val results = TestcaseRunner(testcase!!.testVectors.script!!, circuit).run()
				TestRunResultPanelSwing.showAsDialog(results)
			} catch (e: Throwable) {
				// TODO Catch syntax errors etc.
				throw e
			}
		}
	}
}