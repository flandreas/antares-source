package ch.scorpion.jabbah.graph.view

import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.draw.InputEventHandler
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.execution.actor.ActorInteractionContext
import ch.scorpion.jabbah.execution.actor.ActorInteractionHandler
import ch.scorpion.jabbah.execution.actor.ClickableActorInteractionHandlerAdapter
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