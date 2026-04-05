package io.antarescircuit.antares.view.port

import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.view.container.DigitalPortViewComponent
import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.graph.container.PortViewComponent
import io.antarescircuit.jabbah.graph.model.Port
import io.antarescircuit.jabbah.graph.model.PortType
import io.antarescircuit.jabbah.graph.view.port.PortView
import io.antarescircuit.jabbah.graph.view.port.PortViewFactory

class DigitalPortViewFactory(private val styleProvider: StyleProvider) : PortViewFactory {

	override fun <T : Any> createPortView(port: Port<T>, direction: Direction?): PortView<T> =
		when(port.portType) {
			PortType.INPUT -> DigitalPortView(
				styleProvider = styleProvider,
				port = port as Port<DigitalSignal>,
				direction = direction ?: Direction.WEST)
			PortType.OUTPUT, PortType.INOUT -> DigitalPortView(
				styleProvider = styleProvider,
				port = port as Port<DigitalSignal>,
				direction = direction ?: Direction.EAST)
		} as PortView<T>

	override fun <T : Any> createPortViewComponent(portView: PortView<T>): PortViewComponent<T> =
		DigitalPortViewComponent(styleProvider, portView as DigitalPortView) as PortViewComponent<T>
}