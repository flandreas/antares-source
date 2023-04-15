package ch.scorpion.jabbah.graph.view.oscilloscope

import io.mockk.every
import io.mockk.mockk

class OscilloscopeViewFactoryMockBuilder {

	private val factory = mockk<OscilloscopeViewFactory>()
	private val signalHistoryDrawer: SignalHistoryDrawer<Any> = mockk(relaxed = true)
	private val timelineView: SignalHistoryTimelineView = mockk(relaxed = true)
	private val yAxis: SignalHistoryYAxis<Any> = mockk(relaxed = true)

	init {
		every { factory.getRowHeight(any()) } returns 20
		every { factory.createSignalHistoryDrawer(any(), any()) } returns signalHistoryDrawer
		every { factory.createSignalHistoryTimelineView() } returns timelineView
		every { factory.createSignalHistoryYAxis(any()) } returns yAxis
	}

	fun build(): OscilloscopeViewFactory = factory
}