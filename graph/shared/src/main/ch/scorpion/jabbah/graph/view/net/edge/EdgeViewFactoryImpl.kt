package ch.scorpion.jabbah.graph.view.net.edge

import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.execution.speed.CurrentSystemSpeedCategory
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.NetView

/**
 * Standard implementation of the [EdgeViewFactory] interface.
 */
open class EdgeViewFactoryImpl<T : Any>(
	private val styleProvider: StyleProvider,
	private val currentSystemSpeedCategory: CurrentSystemSpeedCategory
) : EdgeViewFactory<T> {

	override fun createEdgeView(): EdgeView<T> =
		EdgeViewImpl(styleProvider, currentSystemSpeedCategory)

	override fun createEdgeView(netView: NetView<T>): EdgeView<T> =
		EdgeViewImpl(styleProvider, currentSystemSpeedCategory, netView.net)
}