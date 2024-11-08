package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.execution.issue.IssuesView
import ch.scorpion.jabbah.execution.issue.IssuesViewController
import dev.mokkery.MockMode
import dev.mokkery.mock

// TODO: This should be located in the execution module, but cannot yet be imported as test artefact
class IssuesViewMockBuilder(controller: IssuesViewController)  {

	private val view = mock<IssuesView>(MockMode.autofill)

	init {
		controller.view = view
	}

	fun build(): IssuesView = view
}