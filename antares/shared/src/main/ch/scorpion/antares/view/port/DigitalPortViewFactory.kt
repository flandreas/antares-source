package ch.scorpion.antares.view.port

import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.view.container.DigitalPortViewComponent
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.graph.container.PortViewComponent
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.view.port.PortView
import ch.scorpion.jabbah.graph.view.port.PortViewFactory

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