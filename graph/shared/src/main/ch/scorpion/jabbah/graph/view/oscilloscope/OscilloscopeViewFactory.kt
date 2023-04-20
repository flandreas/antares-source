package ch.scorpion.jabbah.graph.view.oscilloscope

import ch.scorpion.jabbah.graph.model.GraphType

/** A factory for creating various objects used by [OscilloscopeView].*/
interface OscilloscopeViewFactory {

    /**
     * Returns the height of the drawing area used by a [SignalHistoryDrawer]
     * created by [createSignalHistoryDrawer].
     */
    fun getRowHeight(graphType: GraphType): Int

	fun createSignalHistoryYAxis(graphType: GraphType): SignalHistoryYAxis<*>?

    /** Creates a new [SignalHistoryDrawer].*/
    fun createSignalHistoryDrawer(graphType: GraphType, yAxis: SignalHistoryYAxis<*>?, rightInset: Int): SignalHistoryDrawer<Any>

	fun createSignalHistoryTimelineView(rightInset: Int): SignalHistoryTimelineView
}

class UndefinedOscilloscopeViewFactory : OscilloscopeViewFactory {

	override fun getRowHeight(graphType: GraphType): Int = 0

	override fun createSignalHistoryYAxis(graphType: GraphType): SignalHistoryYAxis<*>? = null

    override fun createSignalHistoryDrawer(graphType: GraphType, yAxis: SignalHistoryYAxis<*>?, rightInset: Int): SignalHistoryDrawer<Any> {
        throw UnsupportedOperationException("not implemented")
    }

	override fun createSignalHistoryTimelineView(rightInset: Int): SignalHistoryTimelineView {
		throw UnsupportedOperationException("not implemented")
	}
}