package ch.scorpion.jabbah.graph.view.port

import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.graph.container.PortViewComponent
import ch.scorpion.jabbah.graph.model.Port

/** A [PortViewFactory] implementation used for testing. */
class TestPortViewFactory : PortViewFactory {

	override fun <T : Any> createPortView(port: Port<T>, direction: Direction?): PortView<T> =
		TestPortView(port, direction ?: Direction.WEST, PortLabelPosition.EXTERNAL, 0)

	override fun <T : Any> createPortViewComponent(portView: PortView<T>): PortViewComponent<T> =
        PortViewComponent(DrawStyleModule.styleProvider, portView)
}