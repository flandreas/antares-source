package ch.scorpion.antares.view.oscilloscope

import ch.scorpion.jabbah.graph.view.oscilloscope.OscilloscopeViewFactory
import ch.scorpion.jabbah.graph.view.oscilloscope.SignalHistoryDrawer

class DigitalOscilloscopeViewFactory : OscilloscopeViewFactory {

    override val rowHeight: Int get() = 50

    override fun createSignalHistoryDrawer(): SignalHistoryDrawer {
        return DigitalSignalHistoryDrawer()
    }
}