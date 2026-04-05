package io.antarescircuit.jabbah.graph.view.port

import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.graph.container.PortViewComponent
import io.antarescircuit.jabbah.graph.model.Port

/**
 * A factory for creating various instances of [PortView] related classes.
 */
interface PortViewFactory {

	fun <T: Any> createPortView(port: Port<T>, direction: Direction? = null): PortView<T>

	fun <T: Any> createPortViewComponent(portView: PortView<T>): PortViewComponent<T>

}

class UndefinedPortViewFactory : PortViewFactory {

	override fun <T : Any> createPortView(port: Port<T>, direction: Direction?): PortView<T> {
		throw UnsupportedOperationException("not implemented")
	}

	override fun <T : Any> createPortViewComponent(portView: PortView<T>): PortViewComponent<T> {
		throw UnsupportedOperationException("not implemented")
	}
}