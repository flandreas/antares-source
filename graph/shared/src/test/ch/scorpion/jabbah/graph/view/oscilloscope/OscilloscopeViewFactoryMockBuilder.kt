package ch.scorpion.jabbah.graph.view.oscilloscope

import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock


class OscilloscopeViewFactoryMockBuilder {

	private val factory = mock<OscilloscopeViewFactory>()
	private val signalHistoryDrawer: SignalHistoryDrawer<Any> = mock(MockMode.autofill)
	private val timelineView: SignalHistoryTimelineView = mock(MockMode.autofill)
	private val yAxis: SignalHistoryYAxis<Any> = mock(MockMode.autofill)

	init {
		every { factory.getRowHeight(any()) } returns 20
		every { factory.createSignalHistoryDrawer(any(), any(), any()) } returns signalHistoryDrawer
		every { factory.createSignalHistoryTimelineView(any()) } returns timelineView
		every { factory.createSignalHistoryYAxis(any()) } returns yAxis
	}

	fun build(): OscilloscopeViewFactory = factory
}