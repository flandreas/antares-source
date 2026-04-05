package io.antarescircuit.jabbah.graph.ui

import io.antarescircuit.jabbah.graph.ui.logview.LogView
import io.antarescircuit.jabbah.graph.ui.logview.LogViewController
import dev.mokkery.MockMode
import dev.mokkery.mock

class LogViewMockBuilder(controller: LogViewController) {

	private val view = mock<LogView>(MockMode.autofill)

	fun build(): LogView = view
}