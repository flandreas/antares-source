package ch.scorpion.jabbah.graph.view.oscilloscope

import io.mockk.every
import io.mockk.mockk

class OscilloscopeViewFactoryMockBuilder {

	private val factory = mockk<OscilloscopeViewFactory>()
	private val signalHistoryDrawer: SignalHistoryDrawer = mockk(relaxed = true)
	private val timelineView: SignalHistoryTimelineView = mockk(relaxed = true)

	init {
		every { factory.rowHeight } returns 20
		every { factory.createSignalHistoryDrawer() } returns signalHistoryDrawer
		every { factory.createSignalHistoryTimelineView() } returns timelineView
	}

	fun build(): OscilloscopeViewFactory = factory
}