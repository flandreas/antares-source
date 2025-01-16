package ch.scorpion.jabbah.graph.view.oscilloscope

import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.graph.model.GenericGraphType
import ch.scorpion.jabbah.graph.model.GraphType
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.model.oscilloscope.OscilloscopeProbeVertice
import ch.scorpion.jabbah.graph.view.EdgeView

/** A factory for creating various objects used by [OscilloscopeView].*/
interface OscilloscopeViewFactory {

    /**
     * Returns the height of the drawing area used by a [SignalHistoryDrawer]
     * created by [createSignalHistoryDrawer].
     */
    fun getRowHeight(graphType: GraphType): Int

	fun createSignalHistoryYAxis(graphType: GraphType, port: Port<*>): SignalHistoryYAxis<*>?

    /** Creates a new [SignalHistoryDrawer].*/
    fun createSignalHistoryDrawer(graphType: GraphType, yAxis: SignalHistoryYAxis<*>?, rightInset: Int): SignalHistoryDrawer<Any>

	fun createSignalHistoryTimelineView(rightInset: Int): SignalHistoryTimelineView

    fun <T: Any> createProbeVerticeView(
        name: String = "",
        graphType: GraphType = GenericGraphType,
        color: CompositeColor = CompositeColor(),
        model: OscilloscopeProbeVertice<T> = OscilloscopeProbeVertice.create(name, graphType),
        dragGhost: Boolean = false,
        styleProvider: StyleProvider = DrawStyleModule.styleProvider
    ): OscilloscopeProbeVerticeView<T>

    fun completeSignal(probe: OscilloscopeProbeVertice<*>, signal: Any, edgeView: EdgeView<*>): Any = signal
}

class UndefinedOscilloscopeViewFactory : OscilloscopeViewFactory {

	override fun getRowHeight(graphType: GraphType): Int = 0

	override fun createSignalHistoryYAxis(graphType: GraphType, port: Port<*>): SignalHistoryYAxis<*>? = null

    override fun createSignalHistoryDrawer(graphType: GraphType, yAxis: SignalHistoryYAxis<*>?, rightInset: Int): SignalHistoryDrawer<Any> {
        throw UnsupportedOperationException("not implemented")
    }

	override fun createSignalHistoryTimelineView(rightInset: Int): SignalHistoryTimelineView {
		throw UnsupportedOperationException("not implemented")
	}

    override fun <T : Any> createProbeVerticeView(
        name: String,
        graphType: GraphType,
        color: CompositeColor,
        model: OscilloscopeProbeVertice<T>,
        dragGhost: Boolean,
        styleProvider: StyleProvider
    ): OscilloscopeProbeVerticeView<T> {
        throw UnsupportedOperationException("not implemented")
    }
}