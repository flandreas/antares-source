package io.antarescircuit.jabbah.graph.view.port

import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.graph.container.PortViewComponent
import io.antarescircuit.jabbah.graph.model.Port

/** A [PortViewFactory] implementation used for testing. */
class TestPortViewFactory : PortViewFactory {

	override fun <T : Any> createPortView(port: Port<T>, direction: Direction?): PortView<T> =
		TestPortView(port, direction ?: Direction.WEST, PortLabelPosition.EXTERNAL, 0)

	override fun <T : Any> createPortViewComponent(portView: PortView<T>): PortViewComponent =
        PortViewComponent(DrawStyleModule.styleProvider, portView)
}