package ch.scorpion.jabbah.graph.view.vertice

import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.MutableRectangularShape
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.execution.actor.ActorInteractionContext
import ch.scorpion.jabbah.execution.actor.ActorInteractionHandler
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.TestControlVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeLink
import ch.scorpion.jabbah.graph.view.ControlView
import ch.scorpion.jabbah.graph.view.ControlViewSource
import ch.scorpion.jabbah.graph.view.port.TestPortView
import ch.scorpion.jabbah.graph.view.port.PortLabelPosition
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

/** A [ControlView] implementation used for unit and integration testing.*/
class TestControlVerticeView(
	vertice: TestControlVertice = TestControlVertice(),
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	rectangle: MutableRectangularShape = Rectangle2D(0, 0, 100, 100)
) : AbstractRectangularVerticeView<TestControlVertice>(styleProvider, vertice, rectangle),
	ControlView<TestControlVertice>,
	ControlViewSource<TestControlVertice>
{

	var actorInteractionHandler: ActorInteractionHandler? = null

	init {
		modelExchanged(null)
	}

	override fun getActorInteractionHandler(context: ActorInteractionContext): ActorInteractionHandler =
		actorInteractionHandler ?: super.getActorInteractionHandler(context)

	override fun modelExchanged(oldModel: TestControlVertice?) {
		super.modelExchanged(oldModel)
		addPortView(TestPortView(model.getInput<Boolean>(), Direction.WEST, PortLabelPosition.INTERNAL, 0))
	}

	/** ---- [ControlView] */

	override var isActiveControlView: Boolean = false

	override val controlId: String get() = "$type \"${model.name}\""

	override fun bindControlView(subGraphVerticeView: SubGraphVerticeView<*>, link: VerticeLink, startGraph: Graph) {
		this.model = link.getLinkedVertice(startGraph) as TestControlVertice
	}

	override fun sourcePropertiesChanged(source: ControlViewSource<TestControlVertice>) {
		// empty
	}

	override fun writeModelProperties(writer: StoreWriter) { }

	override fun readModelProperties(reader: StoreReader) { }

	/** ---- [ControlViewSource] */

	override val controlName: String get() = super.controlName

	override val iconPath: String get() = "not used"

	override fun createControlView(): ControlView<TestControlVertice> =
		TestControlVerticeView(model, styleProvider, bounds)
}