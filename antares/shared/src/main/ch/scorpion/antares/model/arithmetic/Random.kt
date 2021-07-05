package ch.scorpion.antares.model.arithmetic

import ch.scorpion.antares.model.Trigger
import ch.scorpion.antares.model.gate.AbstractDigitalGate
import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.*
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.model.text.Translation
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter
import kotlin.random.nextULong

/**
 * Produces a random value of a specifiable [BitWidth] when the trigger input value changes to 1.
 */
class Random(
	private val valueProvider: (ULong) -> ULong = { kotlin.random.Random.nextULong(0UL, it) }
) : CalculatingVertice(CALCULATOR), DigitalSignalSource {

	companion object {

		private const val BASE_RESOURCE_KEY = "library.element.Random"
		private val TYPE get() = Translations.getString("$BASE_RESOURCE_KEY.name")
		private val TYPE_DESC get() = Translations.getOptionalString("$BASE_RESOURCE_KEY.desc")

		private val CLOCK_PORT_DESC get() = TranslatableText(Translation.ofStaticKey("antares.random.clockPort.desc"))
		private val DATA_PORT_DESC get() = TranslatableText(Translation.ofStaticKey("antares.random.dataPort.desc"))

		private val CALCULATOR = Calculator()

		private class Calculator : VerticeCalculator<Random> {
			override fun calculate(vertice: Random, data: GraphActorData, signalHandler: SignalHandler) {
				if (data.getSignal<DigitalSignal>(1)!!.bitAt(0) == Bit.True) {
					vertice.getOutput<DigitalSignal>().setOutgoingSignalBuffered(
						DigitalSignalFactory.of(vertice.bitWidth, vertice.valueProvider.invoke(vertice.bitWidth.maxValue)),
						signalHandler
					)
				}
			}
		}
	}

	override val type: String get() = TYPE
	override val typeDesc: String? get() = TYPE_DESC

	init {
		propagationDelay = AbstractDigitalGate.DEFAULT_PROPAGATION_DELAY
		addPort(DigitalPortImpl(portType = PortType.INPUT, trigger = Trigger.EDGE, name = null, description = CLOCK_PORT_DESC))
		addPort(DigitalPortImpl(portType = PortType.OUTPUT, name = null, bitWidth = BitWidth.BW_8, description = DATA_PORT_DESC))
	}

	/** [DigitalSignalSource] interface */

	override var bitWidth: BitWidth
		get() = (getOutput<DigitalSignal>() as DigitalPort).bitWidth
		set(value) {
			if (value != bitWidth) {
				(getOutput<DigitalSignal>() as DigitalPort).bitWidth = value
				stateChanged()
			}
		}

	override var signal: DigitalSignal?
		get() = getOutput<DigitalSignal>().getOutgoingSignal()
		set(@Suppress("UNUSED_PARAMETER") value) {
			throw UnsupportedOperationException()
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