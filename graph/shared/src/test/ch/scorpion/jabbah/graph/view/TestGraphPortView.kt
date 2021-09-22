package ch.scorpion.jabbah.graph.view

import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.graph.model.GraphPort
import ch.scorpion.jabbah.graph.model.port.PortImpl
import ch.scorpion.jabbah.graph.model.vertice.GraphInputImpl
import ch.scorpion.jabbah.graph.model.vertice.GraphOutputImpl
import ch.scorpion.jabbah.graph.view.port.PortLabelPosition
import ch.scorpion.jabbah.graph.view.port.TestPortView
import ch.scorpion.jabbah.graph.view.vertice.AbstractVerticeView

/** An implementation of the [GraphPortView] interface used for testing.*/
class TestGraphPortView<T : Any>(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: GraphPort<T> = GraphInputImpl()
) : AbstractVerticeView<GraphPort<T>>(styleProvider, model), GraphPortView<GraphPort<T>> {

	companion object {

		fun <T : Any> input(name: String): TestGraphPortView<T> = TestGraphPortView(model = GraphInputImpl(PortImpl.createOutput(name = name), name = name))
		fun <T : Any> output(name: String): TestGraphPortView<T> = TestGraphPortView(model = GraphOutputImpl(PortImpl.createInput(name = name), name = name))
	}

	init {
		addPortView(TestPortView<Boolean>(model.getPort(), Direction.WEST, PortLabelPosition.EXTERNAL, 0))
	}

	override val iconPath: String get() = "icon"

	override fun getBoundingBoxImpl(): Rectangle2D = Rectangle2D()

	override fun contains(x: Double, y: Double): Boolean = false

	override var location: Point2D = Point2D.ZERO
}