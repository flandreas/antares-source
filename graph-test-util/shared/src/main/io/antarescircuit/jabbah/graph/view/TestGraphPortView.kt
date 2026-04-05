package io.antarescircuit.jabbah.graph.view

import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.draw.InputEventHandler
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.execution.actor.ActorInteractionContext
import io.antarescircuit.jabbah.execution.actor.ActorInteractionHandler
import io.antarescircuit.jabbah.execution.actor.ClickableActorInteractionHandlerAdapter
import io.antarescircuit.jabbah.graph.model.GraphPort
import io.antarescircuit.jabbah.graph.model.port.PortImpl
import io.antarescircuit.jabbah.graph.model.vertice.GraphInputImpl
import io.antarescircuit.jabbah.graph.model.vertice.GraphOutputImpl
import io.antarescircuit.jabbah.graph.view.port.PortLabelPosition
import io.antarescircuit.jabbah.graph.view.port.TestPortView
import io.antarescircuit.jabbah.graph.view.vertice.AbstractVerticeView

/** An implementation of the [GraphPortView] interface used for testing.*/
class TestGraphPortView<T : Any>(
    styleProvider: StyleProvider = DrawStyleModule.styleProvider,
    model: GraphPort<T> = GraphInputImpl()
) : AbstractVerticeView<GraphPort<T>>(styleProvider, model), GraphPortView<GraphPort<T>> {

	companion object {

		fun <T : Any> input(name: String): TestGraphPortView<T> = TestGraphPortView(model = GraphInputImpl(
            PortImpl.Companion.createOutput(
                name = name
            ), name = name
        )
        )
		fun <T : Any> output(name: String): TestGraphPortView<T> = TestGraphPortView(model = GraphOutputImpl(
            PortImpl.Companion.createInput(
                name = name
            ), name = name
        )
        )
	}

	private val actorInteractionHandler = InteractionHandler()

	init {
		modelExchanged(null)
	}

	override fun modelExchanged(oldModel: GraphPort<T>?) {
		super.modelExchanged(oldModel)
		addPortView(TestPortView<Boolean>(model.getPort(), Direction.WEST, PortLabelPosition.EXTERNAL, 0))
	}

	override val iconPath: String get() = "icon"

	override fun getBoundingBoxImpl(): Rectangle2D = Rectangle2D(location.x, location.y, 20.0, 20.0)

	override fun contains(x: Double, y: Double): Boolean = boundingBox.contains(x, y)

	override var location: Point2D = Point2D.Companion.ZERO

	override fun getActorInteractionHandler(context: ActorInteractionContext): ActorInteractionHandler =
		actorInteractionHandler

	private inner class InteractionHandler : ClickableActorInteractionHandlerAdapter() {
		override fun mousePressed(context: ActorInteractionContext): InputEventHandler<ActorInteractionContext>? {
			(model as GraphInputImpl<T>).handleClick(context.signalHandler)
			return null
		}
	}
}