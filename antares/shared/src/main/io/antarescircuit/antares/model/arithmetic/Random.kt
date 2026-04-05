package io.antarescircuit.antares.model.arithmetic

import io.antarescircuit.antares.model.Trigger
import io.antarescircuit.antares.model.gate.AbstractLogicGate
import io.antarescircuit.antares.model.port.DigitalPort
import io.antarescircuit.antares.model.port.DigitalPortImpl
import io.antarescircuit.antares.model.signal.*
import io.antarescircuit.antares.model.vertice.AdjustableBitWidth
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.edit.model.text.Translation
import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.graph.model.Graph
import io.antarescircuit.jabbah.graph.model.GraphActorData
import io.antarescircuit.jabbah.graph.model.GraphElement
import io.antarescircuit.jabbah.graph.model.PortType
import io.antarescircuit.jabbah.graph.model.vertice.CalculatingVertice
import io.antarescircuit.jabbah.graph.model.vertice.VerticeCalculator
import io.antarescircuit.jabbah.io.Storable
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter
import kotlin.random.nextULong

/**
 * Produces a random value of a specifiable [BitWidth] when the trigger input value changes to 1.
 */
class Random(
	private val valueProvider: (ULong) -> ULong = { kotlin.random.Random.nextULong(0UL, it + 1UL) }
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
		propagationDelay = AbstractLogicGate.DEFAULT_PROPAGATION_DELAY
		addPort(DigitalPortImpl(portType = PortType.INPUT, trigger = Trigger.EDGE, name = null, description = CLOCK_PORT_DESC))
		addPort(DigitalPortImpl(portType = PortType.OUTPUT, name = null, bitWidth = BitWidth.BW_8, description = DATA_PORT_DESC))
	}

	/** ---- [GraphElement] */

	override fun graphParamsChanged(graph: Graph) {
		super.graphParamsChanged(graph)
		(bitWidth as? BitWidthExpression)?.let { it.evaluateIn(graph)?.let { bw -> bitWidth = bw } }
	}

	/** ---- [AdjustableBitWidth] */

	override fun adjustBitWidth(portId: Int, bitWidth: BitWidth): Boolean {
		this.bitWidth = bitWidth
		return true
	}

	/** [DigitalSignalSource] interface */

	override var fixedPointConfig: FixedPointConfig? = null

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
		bitWidth.write("bitWidth", writer)
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		bitWidth = BitWidth.read("bitWidth", reader)
	}
}