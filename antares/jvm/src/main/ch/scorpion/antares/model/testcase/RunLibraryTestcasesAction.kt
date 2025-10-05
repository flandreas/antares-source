package ch.scorpion.antares.model.testcase

import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.invocation.InvocationHandler
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.edit.auth.Operation
import ch.scorpion.jabbah.edit.model.ComponentMessage
import ch.scorpion.jabbah.edit.model.ComponentMessageType
import ch.scorpion.jabbah.graph.library.AbstractLibraryDirectoryAction
import ch.scorpion.jabbah.graph.library.Library
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewController

/**
 * Runs all [Testcase]s of all [DigitalGraph]s in a [Library]
 * and posts [DisplayTestRunResults] on the [EventBus].
 */
class RunLibraryTestcasesAction(
	controller: LibraryTreeViewController
) : AbstractLibraryDirectoryAction(
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
			val results = TestcaseService.runAllLibraryTests(selectedFolder.library!!)

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