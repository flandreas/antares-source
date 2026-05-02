package io.antarescircuit.antares.model.net

import io.antarescircuit.antares.model.Logic
import io.antarescircuit.antares.model.port.DigitalPort
import io.antarescircuit.antares.model.port.DigitalPortImpl
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.model.signal.DigitalSignalFactory
import io.antarescircuit.antares.model.vertice.AdjustableBitWidth
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.execution.scheduler.BreakEvent
import io.antarescircuit.jabbah.graph.model.GraphActorData
import io.antarescircuit.jabbah.graph.model.GraphElement
import io.antarescircuit.jabbah.graph.model.SignalUtil
import io.antarescircuit.jabbah.graph.model.vertice.CalculatingVertice
import io.antarescircuit.jabbah.graph.model.vertice.VerticeCalculator
import io.antarescircuit.jabbah.io.Storable
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter
import io.antarescircuit.jabbah.execution.actor.Actor


class Break(
	bitWidth: BitWidth = BitWidth.BW_1,
	value: DigitalSignal = DigitalSignalFactory.falseValue(bitWidth),
	private val eventBus: EventBus = BaseModule.eventBus
) : CalculatingVertice(CALCULATOR), AdjustableBitWidth {

	companion object {
		private val LOG by logger(Break::class)

		private val CALCULATOR = Calculator()
		private const val BASE_RESOURCE_KEY = "library.element.Break"
		private val TYPE get() = Translations.getString("$BASE_RESOURCE_KEY.name")
		private val TYPE_DESC get() = Translations.getOptionalString("$BASE_RESOURCE_KEY.desc")

		private class Calculator : VerticeCalculator<Break> {
			override fun calculate(vertice: Break, data: GraphActorData, signalHandler: SignalHandler) {
				vertice.handleInputChanged(data, signalHandler)
			}
		}
	}

	init {
		addPort(DigitalPortImpl.createInOut(Logic.POSITIVE, null, bitWidth))
	}

	var value: DigitalSignal = value
		set(value) {
			if (field != value) {
				field = value
				stateChanged()
			}
		}

	var bitWidth: BitWidth
		get() = (getInput<DigitalSignal>() as DigitalPort).bitWidth
		set(value) {
			if (value != bitWidth) {
				(getInput<DigitalSignal>() as DigitalPort).bitWidth = value
				stateChanged()
			}
		}

	var isTriggered = false
		private set

	/** ---- [GraphElement] interface */

	override val type: String get() = TYPE

	override val typeDesc: String? get() = TYPE_DESC

	override fun adjustBitWidth(portId: Int, bitWidth: BitWidth): Boolean {
		this.bitWidth = bitWidth
		return true
	}

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		if (bitWidth.width > 1) {
			bitWidth.write("bitWidth", writer)
		}
		writer.writeULong("value", value.getValue())
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		if (reader.hasAttribute("bitWidth")) {
			bitWidth = BitWidth.read("bitWidth", reader)
		}
		if (reader.hasAttribute("logic")) {
			val logic = Logic.withName(reader.readString("logic"))
			value = when (logic) {
                Logic.POSITIVE -> DigitalSignalFactory.of(true)
                Logic.NEGATIVE -> DigitalSignalFactory.of(false)
            }
		}
		if (reader.hasAttribute("value")) {
			value = DigitalSignalFactory.of(bitWidth, reader.readULong("value"))
		}
	}

	/** ---- [Actor] */

	override fun executionStart(signalHandler: SignalHandler) {
		super.executionStart(signalHandler)
		isTriggered = false
	}

	/** ---- [Break] */

	private fun handleInputChanged(data: GraphActorData, signalHandler: SignalHandler) {
		if (isTriggered(data)) {
			isTriggered = true
			issueBreak(signalHandler)
			stateChanged(signalHandler)
		} else {
			isTriggered = false
		}
	 }

	private fun isTriggered(data: GraphActorData): Boolean =
		SignalUtil.equals(value, data.getSignal(1))

	private fun issueBreak(signalHandler: SignalHandler) {
		LOG.trace("Break at ${signalHandler.executionTime} ns")
		eventBus.post(BreakEvent())
	}
}