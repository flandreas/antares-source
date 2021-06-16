package ch.scorpion.jabbah.graph.ui.logview

import io.mockk.mockk

class LogViewMockBuilder(private val controller: LogViewController) {

	private val view = mockk<LogView>(relaxed = true)

	fun build(): LogView = view
}