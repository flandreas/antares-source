package ch.scorpion.antares.view.oscilloscope

import ch.scorpion.antares.model.AntaresGraphTypes
import ch.scorpion.antares.view.analog.AnalogSignalHistoryDrawer
import ch.scorpion.jabbah.graph.model.GraphType
import ch.scorpion.jabbah.graph.view.oscilloscope.OscilloscopeViewFactory
import ch.scorpion.jabbah.graph.view.oscilloscope.SignalHistoryDrawer
import ch.scorpion.jabbah.graph.view.oscilloscope.SignalHistoryTimelineView
import ch.scorpion.jabbah.graph.view.oscilloscope.SignalHistoryTimelineViewImpl

class AntaresOscilloscopeViewFactory : OscilloscopeViewFactory {

    override val rowHeight: Int get() = DigitalSignalHistoryDrawer.ROW_HEIGHT

    override fun createSignalHistoryDrawer(graphType: GraphType): SignalHistoryDrawer<Any> =
		when (graphType) {
		    AntaresGraphTypes.Digital -> DigitalSignalHistoryDrawer() as SignalHistoryDrawer<Any>
		    AntaresGraphTypes.Analog -> AnalogSignalHistoryDrawer() as SignalHistoryDrawer<Any>
		    else -> throw IllegalArgumentException("unknown GraphType $graphType")
	    }

	override fun createSignalHistoryTimelineView(): SignalHistoryTimelineView = SignalHistoryTimelineViewImpl()
}