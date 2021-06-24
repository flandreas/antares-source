package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.execution.issue.IssuesView
import ch.scorpion.jabbah.execution.issue.IssuesViewController
import io.mockk.mockk

// TODO: This should be located in the execution module, but cannot yet be imported as test artefact
class IssuesViewMockBuilder(controller: IssuesViewController)  {

	private val view = mockk<IssuesView>(relaxed = true)

	init {
		controller.view = view
	}

	fun build(): IssuesView = view
}