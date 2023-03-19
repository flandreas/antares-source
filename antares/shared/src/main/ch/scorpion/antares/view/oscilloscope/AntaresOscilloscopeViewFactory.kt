package ch.scorpion.antares.view.oscilloscope

import ch.scorpion.antares.model.AntaresGraphTypes.Analog
import ch.scorpion.antares.model.AntaresGraphTypes.Digital
import ch.scorpion.antares.view.analog.AnalogSignalHistoryDrawer
import ch.scorpion.antares.view.analog.AnalogSignalHistoryYAxis
import ch.scorpion.jabbah.graph.model.GraphType
import ch.scorpion.jabbah.graph.view.oscilloscope.*

class AntaresOscilloscopeViewFactory : OscilloscopeViewFactory {

	override fun getRowHeight(graphType: GraphType): Int =
		when (graphType) {
			Digital -> DigitalSignalHistoryDrawer.ROW_HEIGHT
			Analog -> AnalogSignalHistoryDrawer.ROW_HEIGHT
			else -> throw IllegalArgumentException("unknown GraphType $graphType")
		}

	override fun createSignalHistoryYAxis(graphType: GraphType): SignalHistoryYAxis<*>? =
		when (graphType) {
			Digital -> null
			Analog -> AnalogSignalHistoryYAxis()
			else -> throw IllegalArgumentException("unknown GraphType $graphType")
		}

    override fun createSignalHistoryDrawer(graphType: GraphType, yAxis: SignalHistoryYAxis<*>?): SignalHistoryDrawer<Any> =
		when (graphType) {
		    Digital -> DigitalSignalHistoryDrawer() as SignalHistoryDrawer<Any>
		    Analog -> AnalogSignalHistoryDrawer(yAxis as AnalogSignalHistoryYAxis) as SignalHistoryDrawer<Any>
		    else -> throw IllegalArgumentException("unknown GraphType $graphType")
	    }

	override fun createSignalHistoryTimelineView(): SignalHistoryTimelineView = SignalHistoryTimelineViewImpl()
}