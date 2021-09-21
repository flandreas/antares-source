package ch.scorpion.antares.model.net

import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.Actor
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

class Power(
	bitWidth: BitWidth = BitWidth.BW_1
) : CalculatingVertice(CALCULATOR) {

	companion object {

		private const val BASE_RESOURCE_KEY = "library.element.Power"
		private val TYPE get() = Translations.getString("$BASE_RESOURCE_KEY.name")
		private val TYPE_DESC get() = Translations.getOptionalString("$BASE_RESOURCE_KEY.desc")

		private val CALCULATOR = Calculator()

		private class Calculator : VerticeCalculator<Power> {
			override fun calculate(vertice: Power, data: GraphActorData, signalHandler: SignalHandler) {
				vertice.getOutput<DigitalSignal>().setOutgoingSignal(DigitalSignalFactory.trueValue(vertice.bitWidth), signalHandler)
			}
		}
	}

	var bitWidth: BitWidth
		get() = (getOutput<DigitalSignal>() as DigitalPort).bitWidth
		set(newValue) {
			if (newValue != bitWidth) {
				(getOutput<DigitalSignal>() as DigitalPort).bitWidth = newValue
				stateChanged()
			}
		}

	override val type: String get() = TYPE
	override val typeDesc: String? get() = TYPE_DESC

	init {
		addPort(DigitalPortImpl(portType = PortType.OUTPUT, bitWidth = bitWidth))
		propagationDelay = 1
	}

	/** ---- [Actor] */

	override fun executionInitialize(signalHandler: SignalHandler) {
		super.executionInitialize(signalHandler)
		getOutput<DigitalSignal>().setOutgoingSignalBuffered(DigitalSignalFactory.trueValue(bitWidth), signalHandler)
	}

	override fun executionStart(signalHandler: SignalHandler) {
		super.executionStart(signalHandler)
		requestActingAfter(signalHandler, propagationDelay, createActorData(null))
	}

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeInt("bitWidth", bitWidth.width)
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		bitWidth = BitWidth.of(reader.readInt("bitWidth"))
	}
}