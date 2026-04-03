package ch.scorpion.antares.view.addressable

import ch.scorpion.antares.model.addressable.Addressable
import ch.scorpion.antares.model.addressable.LookupTable
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.view.gate.BoxGateView
import ch.scorpion.antares.view.port.AbstractAntaresPortView
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.InputEventContext
import ch.scorpion.jabbah.draw.InputEventHandler
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.execution.actor.ActorInteractionContext
import ch.scorpion.jabbah.execution.actor.ActorInteractionHandler
import ch.scorpion.jabbah.graph.model.GraphElementEvent
import ch.scorpion.jabbah.graph.model.vertice.ImmediateVerticeLink
import ch.scorpion.jabbah.graph.model.vertice.ObjectLink
import ch.scorpion.jabbah.graph.view.port.PortLabelPosition
import ch.scorpion.jabbah.graph.view.vertice.AbstractVerticeView

class LookupTableView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: LookupTable = LookupTable(),
	eventBus: EventBus = BaseModule.eventBus
) : BoxGateView<LookupTable>(styleProvider, "LUT", model, minWidth = 10) {

	private val inputEventHandler = AddressableInputEventHandler(
		eventBus,
	) { view, newDesktopView ->
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