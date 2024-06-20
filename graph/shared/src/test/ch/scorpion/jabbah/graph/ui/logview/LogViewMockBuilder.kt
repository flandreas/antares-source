package ch.scorpion.jabbah.graph.ui.logview

import dev.mokkery.MockMode
import dev.mokkery.mock

class LogViewMockBuilder(private val controller: LogViewController) {

	private val view = mock<LogView>(MockMode.autofill)

	fun build(): LogView = view
}