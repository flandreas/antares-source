package io.antarescircuit.jabbah.graph.view.vertice

import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.base.geom.MutableRectangularShape
import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.execution.actor.ActorInteractionContext
import io.antarescircuit.jabbah.execution.actor.ActorInteractionHandler
import io.antarescircuit.jabbah.graph.model.Graph
import io.antarescircuit.jabbah.graph.model.TestControlVertice
import io.antarescircuit.jabbah.graph.model.vertice.VerticeLink
import io.antarescircuit.jabbah.graph.view.ControlView
import io.antarescircuit.jabbah.graph.view.ControlViewSource
import io.antarescircuit.jabbah.graph.view.port.PortLabelPosition
import io.antarescircuit.jabbah.graph.view.port.TestPortView
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter

/** A [io.antarescircuit.jabbah.graph.view.ControlView] implementation used for unit and integration testing.*/
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

	override val controlId: String get() = "$type:${model.id}"

	override fun bindControlView(subGraphVerticeView: SubGraphVerticeView<*>, link: VerticeLink, startGraph: Graph) {
		this.model = link.getLinkedObject(startGraph) as TestControlVertice
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