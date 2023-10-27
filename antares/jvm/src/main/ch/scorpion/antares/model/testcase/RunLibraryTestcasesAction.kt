package ch.scorpion.antares.model.testcase

import ch.scorpion.antares.model.AntaresGraphTypes
import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.invocation.InvocationHandler
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.auth.Operation
import ch.scorpion.jabbah.edit.model.ComponentMessage
import ch.scorpion.jabbah.edit.model.ComponentMessageType
import ch.scorpion.jabbah.graph.library.AbstractLibraryFolderAction
import ch.scorpion.jabbah.graph.library.Library
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewController

/**
 * Runs all [Testcase]s of all [DigitalGraph]s in a [Library]
 * and posts [DisplayTestRunResults] on the [EventBus].
 */
class RunLibraryTestcasesAction(
	controller: LibraryTreeViewController,
	private val eventBus: EventBus = BaseModule.eventBus
) : AbstractLibraryFolderAction(
	"antares.testcase.action.runAllInLibrary",
	Operation.View,
	controller
) {

	companion object {
		private val LOG by logger(RunLibraryTestcasesAction::class)
	}

	override fun execute(event: ActionEvent) {
		LOG.userTrail("Run all tests in library '${selectedFolder.library?.name?.value}'")

		InvocationHandler.invoke {
			val results = mutableListOf<CombinedTestRunResult>()
			selectedFolder.library?.metaGraphIds?.forEach { uuid ->
				val metaGraph = selectedFolder.library!!.getMetaGraph(uuid)
				if (metaGraph.type == AntaresGraphTypes.Digital) {
					val circuit = metaGraph.graph.model!! as DigitalGraph
					results.addAll(RunTestcaseAction.run(metaGraph, circuit.testcases.testcases))
				}
			}

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