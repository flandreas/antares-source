package io.antarescircuit.antares.view.addressable

import io.antarescircuit.antares.model.addressable.Addressable
import io.antarescircuit.antares.model.addressable.LookupTable
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.view.gate.BoxGateView
import io.antarescircuit.antares.view.port.AbstractAntaresPortView
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.draw.InputEventContext
import io.antarescircuit.jabbah.draw.InputEventHandler
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.execution.actor.ActorInteractionContext
import io.antarescircuit.jabbah.execution.actor.ActorInteractionHandler
import io.antarescircuit.jabbah.graph.model.GraphElementEvent
import io.antarescircuit.jabbah.graph.model.vertice.ImmediateVerticeLink
import io.antarescircuit.jabbah.graph.model.vertice.ObjectLink
import io.antarescircuit.jabbah.graph.view.port.PortLabelPosition
import io.antarescircuit.jabbah.graph.view.vertice.AbstractVerticeView

class LookupTableView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: LookupTable = LookupTable(),
	eventBus: EventBus = BaseModule.eventBus
) : BoxGateView<LookupTable>(styleProvider, "LUT", model, minWidth = 10) {

	private val inputEventHandler = AddressableInputEventHandler(
		eventBus,
	) { view, newDesktopView ->

		@Suppress("UNCHECKED_CAST")
		OpenMemoryContentsRequest(
			view,
			this,
			"LUT",
			ImmediateVerticeLink(this.model.id) as ObjectLink<Addressable>,
			newDesktopView
		)
	}

	init {
		initExternalLabel(Direction.NORTH)
		modelExchanged(null)
	}

	override fun modelExchanged(oldModel: LookupTable?) {
		super.modelExchanged(oldModel)
		addPortView(createInputPortView(model.getAddressInput()))
		createOutputPortView(model.getDataPort()).also {
			it.portLabelPosition = PortLabelPosition.INTERNAL
			addPortView(it)
		}
		updateLayout()
	}

	override val relativeExternalLabelLocation: Point2D
		get() = Point2D(-AbstractAntaresPortView.LENGTH - width / 2, -height / 2 - LABEL_DIST)

	override fun <T : InputEventContext> getInputEventHandler(context: T): InputEventHandler<T> =
		inputEventHandler.getInputEventHandler()

	override fun getActorInteractionHandler(context: ActorInteractionContext): ActorInteractionHandler =
		inputEventHandler.getActorInteractionHandler(this)

	/** ---- UI properties */

	var addressWidth: BitWidth
		get() = model.addressWidth
		set(value) {
			invalidate()
			model.addressWidth = value
			invalidate()
			validate()
		}

	var dataWidth: BitWidth
		get() = model.dataWidth
		set(value) {
			invalidate()
			model.dataWidth = value
			invalidate()
			validate()
		}

	/** ---- [AbstractVerticeView] */

	override fun handleStateChanged(event: GraphElementEvent) {
		if (event.signalHandler == null) {
			invalidate()
			updateLabels()
		}
		super.handleStateChanged(event)
	}
}