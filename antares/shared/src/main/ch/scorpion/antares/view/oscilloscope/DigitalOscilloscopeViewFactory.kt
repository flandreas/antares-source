package ch.scorpion.antares.view.oscilloscope

import ch.scorpion.jabbah.graph.view.oscilloscope.OscilloscopeViewFactory
import ch.scorpion.jabbah.graph.view.oscilloscope.SignalHistoryDrawer
import ch.scorpion.jabbah.graph.view.oscilloscope.SignalHistoryTimelineView
import ch.scorpion.jabbah.graph.view.oscilloscope.SignalHistoryTimelineViewImpl

class DigitalOscilloscopeViewFactory : OscilloscopeViewFactory {

    override val rowHeight: Int get() = DigitalSignalHistoryDrawer.ROW_HEIGHT

    override fun createSignalHistoryDrawer(): SignalHistoryDrawer {
        return DigitalSignalHistoryDrawer()
    }

	override fun createSignalHistoryTimelineView(): SignalHistoryTimelineView {
		return SignalHistoryTimelineViewImpl()
	}
}