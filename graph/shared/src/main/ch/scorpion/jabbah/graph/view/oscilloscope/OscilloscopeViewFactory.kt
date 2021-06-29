package ch.scorpion.jabbah.graph.view.oscilloscope

/** A factory for creating various objects used by [OscilloscopeView].*/
interface OscilloscopeViewFactory {

    /** Returns the height of the drawing area used by a [SignalHistoryDrawer] returned by [createSignalHistoryDrawer].*/
    val rowHeight: Int

    /** Creates a new [SignalHistoryDrawer].*/
    fun createSignalHistoryDrawer(): SignalHistoryDrawer

	fun createSignalHistoryTimelineView(): SignalHistoryTimelineView
}

class UndefinedOscilloscopeViewFactory : OscilloscopeViewFactory {

    override val rowHeight: Int get() = 0

    override fun createSignalHistoryDrawer(): SignalHistoryDrawer {
        throw UnsupportedOperationException("not implemented")
    }

	override fun createSignalHistoryTimelineView(): SignalHistoryTimelineView {
		throw UnsupportedOperationException("not implemented")
	}
}