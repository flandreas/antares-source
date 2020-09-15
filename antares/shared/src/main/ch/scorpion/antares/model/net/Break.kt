package ch.scorpion.antares.model.net

import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.scheduler.BreakEvent
import ch.scorpion.jabbah.graph.model.GraphElement
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter


class Break(
	logic: Logic = Logic.POSITIVE,
	private val eventBus: EventBus = BaseModule.eventBus
) : CalculatingVertice(CALCULATOR) {

	companion object {
		private val LOG by logger(Break::class)

		private val CALCULATOR = Calculator()
		private const val BASE_RESOURCE_KEY = "library.element.Break"
		private val TYPE = Translations.getString("$BASE_RESOURCE_KEY.name")
		private val TYPE_DESC = Translations.getOptionalString("$BASE_RESOURCE_KEY.desc")

		private class Calculator : VerticeCalculator<Break> {
			override fun calculate(vertice: Break, data: GraphActorData, signalHandler: SignalHandler) {
				vertice.handleInputChanged(data, signalHandler)
			}
		}
	}

	init {
		addPort(DigitalPortImpl.createInOut(logic, null, BitWidth.BW_1))
	}

	var logic: Logic = logic
		set(value) {
			if (field != value) {
				field = value
				(getInput<DigitalSignal>() as DigitalPort).logic = field
				stateChanged()
			}
		}

	/** ---- [GraphElement] interface */

	override val type: String get() = TYPE

	override val typeDesc: String? get() = TYPE_DESC

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeString("logic", logic.customName)
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		logic = Logic.withName(reader.readString("logic"))
	}

	/** ---- [Break] */

	val inputSignal: DigitalSignal? get() = getInput<DigitalSignal>().getIncomingSignal()

	val inputSignalSet: Boolean get() = inputSignal?.bitAt(0)?.isSet ?: false

	private fun handleInputChanged(data: GraphActorData, signalHandler: SignalHandler) {
		if (isTriggered(data)) {
			issueBreak(signalHandler)
			stateChanged(signalHandler)
		}
	}

	private fun isTriggered(data: GraphActorData): Boolean {
		val signal = data.getSignal<DigitalSignal>(1)!!.bitAt(0)
		return when (logic) {
			Logic.POSITIVE -> signal.isSet
			Logic.NEGATIVE -> signal.isNotSet
		}
	}

	private fun issueBreak(signalHandler: SignalHandler) {
		LOG.debug("Break at ${signalHandler.executionTime} ns")
		eventBus.post(BreakEvent())
	}
}