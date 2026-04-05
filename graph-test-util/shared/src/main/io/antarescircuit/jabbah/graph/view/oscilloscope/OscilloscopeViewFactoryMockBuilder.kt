package io.antarescircuit.jabbah.graph.view.oscilloscope

import io.antarescircuit.jabbah.draw.graphics.CompositeColor
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.graph.model.GraphType
import io.antarescircuit.jabbah.graph.model.oscilloscope.OscilloscopeProbeVertice
import io.antarescircuit.jabbah.graph.model.oscilloscope.SignalHistoriesType
import dev.mokkery.MockMode
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.matcher.capture.Capture
import dev.mokkery.matcher.capture.capture
import dev.mokkery.matcher.capture.get
import dev.mokkery.mock

class OscilloscopeViewFactoryMockBuilder {

	private val factory = mock<OscilloscopeViewFactory>()
	private val signalHistoryDrawer: SignalHistoryDrawer<Any> = mock(MockMode.autofill)
	private val timelineView: SignalHistoryTimelineView = mock(MockMode.autofill)
	private val yAxis: SignalHistoryYAxis<Any> = mock(MockMode.autofill)

	private val nameSlot = Capture.Companion.slot<String>()
	private val graphTypeSlot = Capture.Companion.slot<GraphType>()
	private val colorSlot = Capture.Companion.slot<CompositeColor>()
	private val modelSlot = Capture.Companion.slot<OscilloscopeProbeVertice<Any>>()
	private val dragGhostSlot = Capture.Companion.slot<Boolean>()

	init {
		every { factory.getRowHeight(any()) } returns 20
		every { factory.createSignalHistoryDrawer(any(), any(), any()) } returns signalHistoryDrawer
		every { factory.createSignalHistoryTimelineView(any()) } returns timelineView
		every { factory.createSignalHistoryYAxis(any(), any()) } returns yAxis
		every { factory.getDefaultMode(any()) } returns SignalHistoriesType.Clocked

		every {
            factory.createProbeVerticeView(
                capture(nameSlot),
                capture(graphTypeSlot),
                capture(colorSlot),
                capture(modelSlot),
                capture(dragGhostSlot),
                any()
            )
        } calls {
			OscilloscopeProbeVerticeView(
				nameSlot.get(),
				graphTypeSlot.get(),
				colorSlot.get(),
				modelSlot.get(),
				dragGhostSlot.get(),
				DrawStyleModule.styleProvider
			)
		}
	}

	fun build(): OscilloscopeViewFactory = factory
}