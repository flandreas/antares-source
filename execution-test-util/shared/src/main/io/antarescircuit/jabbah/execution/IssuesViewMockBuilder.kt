package io.antarescircuit.jabbah.execution

import io.antarescircuit.jabbah.execution.issue.IssuesView
import io.antarescircuit.jabbah.execution.issue.IssuesViewController
import dev.mokkery.MockMode
import dev.mokkery.mock

class IssuesViewMockBuilder(controller: IssuesViewController)  {

	private val view = mock<IssuesView>(MockMode.autofill)

	init {
		controller.view = view
	}

	fun build(): IssuesView = view
}