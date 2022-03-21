package ch.scorpion.antares.view.addressable

import ch.scorpion.antares.model.addressable.LookupTable
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.view.gate.BoxGateView
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.InputEventContext
import ch.scorpion.jabbah.draw.InputEventHandler
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.execution.actor.ActorInteractionContext
import ch.scorpion.jabbah.execution.actor.ActorInteractionHandler
import ch.scorpion.jabbah.graph.view.port.PortLabelPosition

class LookupTableView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: LookupTable = LookupTable(),
	eventBus: EventBus = BaseModule.eventBus
) : BoxGateView<LookupTable>(styleProvider, "LUT", model, minWidth = 10) {

	private val inputEventHandler = AddressableInputEventHandler(
		{ view, newDesktopView -> OpenMemoryContentsRequest(view, this, "LUT", this.model, newDesktopView) },
		eventBus
	)

	init {
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

	override fun <T : InputEventContext> getInputEventHandler(context: T): InputEventHandler<T> =
		inputEventHandler.getInputEventHandler()

	override fun getActorInteractionHandler(context: ActorInteractionContext): ActorInteractionHandler =
		inputEventHandler.getActorInteractionHandler()

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
}