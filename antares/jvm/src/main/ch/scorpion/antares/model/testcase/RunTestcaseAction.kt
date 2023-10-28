package ch.scorpion.antares.model.testcase

import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.module.AntaresModelModule
import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.invocation.InvocationHandler
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.model.ComponentMessage
import ch.scorpion.jabbah.edit.model.ComponentMessageType
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.app.ApplicationModeHolder
import ch.scorpion.jabbah.graph.model.param.GraphParamValues
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

	companion object {
		private val LOG by logger(RunTestcaseAction::class)

		/**
		 * Uses by various types of "Run" actions to run [Testcase] of a particular [MetaGraph].
		 * Encapsulates [DigitalGraph] cloning/disposing and setup of [GraphParamValues].
		 */
		fun run(metaGraph: MetaGraph, testcases: List<Testcase>): List<CombinedTestRunResult> {
			val circuit = metaGraph.graph.model as DigitalGraph

			// Clone circuit to avoid interference from various objects of the main application,
			// such as GraphViewExecutionAnimator that listen on Actors of the main circuit
			val clone = StorableCloner.clone(circuit)

			try {
				// Setup parameter values (generics)
				clone.parameterDefinitions = metaGraph.parameterDefinitions
				clone.parameterValues = GraphParamValues.withDefaults(metaGraph.parameterDefinitions)

				val execScriptAST = circuit.script?.let {
					if (StringUtils.isNotBlank(it)) {
						BaseModule.parserFactory(it, null).parse()
					} else {
						null
					}
				}

				val results = mutableListOf<CombinedTestRunResult>()
				for (testcase in testcases) {
					if (testcase.ignored) {
						results.add(CombinedTestRunResult.ignored(testcase))
					} else {
						results.add(CombinedTestcaseRunner(testcase, clone, execScriptAST) {
							(metaGraph.containerDrawing.getPortViewComponent(it)?.port as DigitalPort?)?.logic
								?: Logic.POSITIVE
						}.run())
					}
				}
				return results
			} finally {
				clone.dispose()
			}
		}
	}

	override fun execute(event: ActionEvent) {
		InvocationHandler.invoke {
			val metaGraph = application.controller.data!!.content as MetaGraph
			val circuit = metaGraph.graph.model as DigitalGraph

			LOG.userTrail("Run testcase '${testcase!!.name.value}' in circuit '${circuit.name.value}'")
			val results = run(metaGraph, listOf(testcase!!))

			val msgType = if (results.any { it.failed }) ComponentMessageType.Error else ComponentMessageType.Info
			eventBus.post(ComponentMessage(msgType, source = null, messageKey = "antares.testcase.results.done.text"))
			eventBus.post(DisplayTestRunResults(results))
		}
	}
}