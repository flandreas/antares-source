package io.antarescircuit.antares.view.oscilloscope

import io.antarescircuit.antares.model.AntaresGraphTypes.Analog
import io.antarescircuit.antares.model.AntaresGraphTypes.Digital
import io.antarescircuit.antares.model.analog.AnalogOscilloscopeProbeVertice
import io.antarescircuit.antares.model.analog.AnalogOscilloscopeSignalType
import io.antarescircuit.antares.model.analog.AnalogSignal
import io.antarescircuit.antares.view.analog.AnalogEdgeView
import io.antarescircuit.antares.view.analog.AnalogSignalHistoryDrawer
import io.antarescircuit.antares.view.analog.AnalogSignalHistoryYAxis
import io.antarescircuit.jabbah.draw.graphics.CompositeColor
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.graph.model.GraphType
import io.antarescircuit.jabbah.graph.model.Port
import io.antarescircuit.jabbah.graph.model.oscilloscope.OscilloscopeProbeVertice
import io.antarescircuit.jabbah.graph.model.oscilloscope.SignalHistoriesType
import io.antarescircuit.jabbah.graph.view.EdgeView
import io.antarescircuit.jabbah.graph.view.oscilloscope.*

@Suppress("UNCHECKED_CAST")
class AntaresOscilloscopeViewFactory : OscilloscopeViewFactory {

	override fun getDefaultMode(graphType: GraphType): SignalHistoriesType =
		when (graphType) {
			Digital -> SignalHistoriesType.Realtime
			Analog -> SignalHistoriesType.Realtime
			else -> throw IllegalArgumentException("unknown GraphType $graphType")
		}

	override fun getRowHeight(graphType: GraphType): Int =
		when (graphType) {
			Digital -> DigitalSignalHistoryDrawer.ROW_HEIGHT
			Analog -> AnalogSignalHistoryDrawer.ROW_HEIGHT
			else -> throw IllegalArgumentException("unknown GraphType $graphType")
		}

	override fun createSignalHistoryYAxis(graphType: GraphType, port: Port<*>): SignalHistoryYAxis<*>? =
		when (graphType) {
			Digital -> null
			Analog -> AnalogSignalHistoryYAxis(port)
			else -> throw IllegalArgumentException("unknown GraphType $graphType")
		}

    override fun createSignalHistoryDrawer(graphType: GraphType, yAxis: SignalHistoryYAxis<*>?, rightInset: Int): SignalHistoryDrawer<Any> =
		when (graphType) {
		    Digital -> DigitalSignalHistoryDrawer(rightInset) as SignalHistoryDrawer<Any>
		    Analog -> AnalogSignalHistoryDrawer(rightInset, yAxis as AnalogSignalHistoryYAxis) as SignalHistoryDrawer<Any>
		    else -> throw IllegalArgumentException("unknown GraphType $graphType")
	    }

	override fun createSignalHistoryTimelineView(rightInset: Int): SignalHistoryTimelineView = SignalHistoryTimelineViewImpl(rightInset)

	override fun <T : Any> createProbeVerticeView(
		name: String,
		graphType: GraphType,
		color: CompositeColor,
		model: OscilloscopeProbeVertice<T>,
		dragGhost: Boolean,
		styleProvider: StyleProvider
	): OscilloscopeProbeVerticeView<T> {
		return when (graphType) {
			Digital -> OscilloscopeProbeVerticeView(name, graphType, color, model, dragGhost, styleProvider)
			Analog -> AnalogOscilloscopeProbeVerticeView(name, color, model as AnalogOscilloscopeProbeVertice, dragGhost, styleProvider) as OscilloscopeProbeVerticeView<T>
			else -> throw IllegalArgumentException("unknown GraphType $graphType")
		}
	}

	override fun completeSignal(probe: OscilloscopeProbeVertice<*>, signal: Any, edgeView: EdgeView<*>): Any =
		if (signal is AnalogSignal && edgeView is AnalogEdgeView && probe is AnalogOscilloscopeProbeVertice) {
			if (probe.signalType == AnalogOscilloscopeSignalType.Current) {
				AnalogSignal(signal.voltage, edgeView.current)
			} else {
				signal
			}
		} else {
			signal
		}
}