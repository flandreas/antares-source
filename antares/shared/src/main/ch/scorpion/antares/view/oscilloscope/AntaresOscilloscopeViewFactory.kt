package ch.scorpion.antares.view.oscilloscope

import ch.scorpion.antares.model.AntaresGraphTypes.Analog
import ch.scorpion.antares.model.AntaresGraphTypes.Digital
import ch.scorpion.antares.model.analog.AnalogOscilloscopeProbeVertice
import ch.scorpion.antares.model.analog.AnalogOscilloscopeSignalType
import ch.scorpion.antares.model.analog.AnalogSignal
import ch.scorpion.antares.view.analog.AnalogEdgeView
import ch.scorpion.antares.view.analog.AnalogSignalHistoryDrawer
import ch.scorpion.antares.view.analog.AnalogSignalHistoryYAxis
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.graph.model.GraphType
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.model.oscilloscope.OscilloscopeProbeVertice
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.oscilloscope.*

class AntaresOscilloscopeViewFactory : OscilloscopeViewFactory {

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