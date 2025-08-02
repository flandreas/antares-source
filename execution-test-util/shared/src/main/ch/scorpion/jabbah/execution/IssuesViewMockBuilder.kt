package ch.scorpion.jabbah.execution

import ch.scorpion.jabbah.execution.issue.IssuesView
import ch.scorpion.jabbah.execution.issue.IssuesViewController
import dev.mokkery.MockMode
import dev.mokkery.mock

class IssuesViewMockBuilder(controller: IssuesViewController)  {

	private val view = mock<IssuesView>(MockMode.autofill)

	init {
		controller.view = view
	}

	fun build(): IssuesView = view
}