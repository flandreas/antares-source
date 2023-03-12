package ch.scorpion.jabbah.graph.view.oscilloscope

import ch.scorpion.jabbah.graph.model.GraphType

/** A factory for creating various objects used by [OscilloscopeView].*/
interface OscilloscopeViewFactory {

    /**
     * Returns the height of the drawing area used by a [SignalHistoryDrawer]
     * created by [createSignalHistoryDrawer].
     */
    fun getRowHeight(graphType: GraphType): Int

    /** Creates a new [SignalHistoryDrawer].*/
    fun createSignalHistoryDrawer(graphType: GraphType): SignalHistoryDrawer<Any>

	fun createSignalHistoryTimelineView(): SignalHistoryTimelineView
}

class UndefinedOscilloscopeViewFactory : OscilloscopeViewFactory {

	override fun getRowHeight(graphType: GraphType): Int = 0

    override fun createSignalHistoryDrawer(graphType: GraphType): SignalHistoryDrawer<Any> {
        throw UnsupportedOperationException("not implemented")
    }

	override fun createSignalHistoryTimelineView(): SignalHistoryTimelineView {
		throw UnsupportedOperationException("not implemented")
	}
}