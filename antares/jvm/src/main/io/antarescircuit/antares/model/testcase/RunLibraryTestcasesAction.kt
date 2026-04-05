package io.antarescircuit.antares.model.testcase

import io.antarescircuit.antares.model.DigitalGraph
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.invocation.InvocationHandler
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.edit.auth.Operation
import io.antarescircuit.jabbah.edit.model.ComponentMessage
import io.antarescircuit.jabbah.edit.model.ComponentMessageType
import io.antarescircuit.jabbah.graph.library.AbstractLibraryDirectoryAction
import io.antarescircuit.jabbah.graph.library.Library
import io.antarescircuit.jabbah.graph.ui.library.LibraryTreeViewController

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