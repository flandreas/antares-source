package ch.scorpion.jabbah.graph.view.oscilloscope

import ch.scorpion.jabbah.graph.model.GraphType

/** A factory for creating various objects used by [OscilloscopeView].*/
interface OscilloscopeViewFactory {

    /** Returns the height of the drawing area used by a [SignalHistoryDrawer] returned by [createSignalHistoryDrawer].*/
    val rowHeight: Int

    /** Creates a new [SignalHistoryDrawer].*/
    fun createSignalHistoryDrawer(graphType: GraphType): SignalHistoryDrawer<Any>

	fun createSignalHistoryTimelineView(): SignalHistoryTimelineView
}

class UndefinedOscilloscopeViewFactory : OscilloscopeViewFactory {

    override val rowHeight: Int get() = 0

    override fun createSignalHistoryDrawer(graphType: GraphType): SignalHistoryDrawer<Any> {
        throw UnsupportedOperationException("not implemented")
    }

	override fun createSignalHistoryTimelineView(): SignalHistoryTimelineView {
		throw UnsupportedOperationException("not implemented")
	}
}