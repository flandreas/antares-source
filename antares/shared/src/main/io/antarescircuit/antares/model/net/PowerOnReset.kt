package io.antarescircuit.antares.model.net

import io.antarescircuit.antares.model.Logic
import io.antarescircuit.antares.model.gate.AbstractLogicGate
import io.antarescircuit.antares.model.port.DigitalPort
import io.antarescircuit.antares.model.port.DigitalPortImpl
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.model.signal.BitWidthExpression
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.model.signal.DigitalSignalFactory
import io.antarescircuit.antares.model.vertice.AdjustableBitWidth
import io.antarescircuit.jabbah.base.LongValueImpl
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.execution.actor.Actor
import io.antarescircuit.jabbah.graph.model.Graph
import io.antarescircuit.jabbah.graph.model.GraphActorData
import io.antarescircuit.jabbah.graph.model.GraphElement
import io.antarescircuit.jabbah.graph.model.Vertice
import io.antarescircuit.jabbah.graph.model.vertice.CalculatingVertice
import io.antarescircuit.jabbah.graph.model.vertice.VerticeCalculator
import io.antarescircuit.jabbah.io.Storable
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter

/**
 * A [Vertice] that outputs a fixed signal during circuit start-up, and then outputs the inverted signal
 * afterward. Used for bringing a (typically sequential) circuit into the same defined state each time
 * the circuit is powered on.
 */
class PowerOnReset(
	bitWidth: BitWidth = BitWidth.BW_1,
	logic: Logic = Logic.POSITIVE
) : CalculatingVertice(CALCULATOR), AdjustableBitWidth {

	companion object {

		private const val BASE_RESOURCE_KEY = "library.element.PowerOnReset"

		private val POS_TYPE_DESC by lazy { Translations.getOptionalString("$BASE_RESOURCE_KEY.pos.desc") }
		private val NEG_TYPE_DESC by lazy { Translations.getOptionalString("$BASE_RESOURCE_KEY.neg.desc") }

		private val CALCULATOR = Calculator()

		private class Calculator : VerticeCalculator<PowerOnReset> {
			override fun calculate(vertice: PowerOnReset, data: GraphActorData, signalHandler: SignalHandler) {
				if (vertice.isPowerOn) {
					vertice.getOutput<DigitalSignal>().setOutgoingSignalBuffered(vertice.powerOnSignal, signalHandler)
					vertice.isPowerOn = false
					vertice.requestActingAfter(signalHandler, vertice.propagationDelay.value, vertice.createActorData(null))
				} else {
					vertice.getOutput<DigitalSignal>().setOutgoingSignalBuffered(vertice.powerOnSignal.not(), signalHandler)
				}
			}
		}
	}

	var bitWidth: BitWidth
		get() = (getOutput<DigitalSignal>() as DigitalPort).bitWidth
		set(value) {
			if (value != bitWidth) {
				(getOutput<DigitalSignal>() as DigitalPort).bitWidth = value
				stateChanged()
			}
		}

	var logic: Logic
		get() = (getOutput<DigitalSignal>() as DigitalPort).logic
		set(value) {
			if (value != logic) {
				(getOutput<DigitalSignal>() as DigitalPort).logic = value
				stateChanged()
			}
		}

	private val powerOnSignal: DigitalSignal get() = logic.evaluate(DigitalSignalFactory.trueValue(bitWidth))

	private lateinit var signal: DigitalSignal

	private var isPowerOn = false

	init {
		addPort(DigitalPortImpl.createOutput(logic, null, bitWidth))
		propagationDelay = LongValueImpl(2 * AbstractLogicGate.DEFAULT_PROPAGATION_DELAY.value)
	}

	/** ---- [GraphElement] */

	override val type: String by lazy { Translations.getString("$BASE_RESOURCE_KEY.name") }

	override val typeDesc: String? get() = when (logic) {
		Logic.POSITIVE -> POS_TYPE_DESC
		Logic.NEGATIVE -> NEG_TYPE_DESC
	}

	override fun graphParamsChanged(graph: Graph) {
		super.graphParamsChanged(graph)
		(bitWidth as? BitWidthExpression)?.let { it.evaluateIn(graph)?.let { bw -> bitWidth = bw } }
	}

	/** ---- [AdjustableBitWidth] */

	override fun adjustBitWidth(portId: Int, bitWidth: BitWidth): Boolean {
		this.bitWidth = bitWidth
		return true
	}

	/** ---- [Actor] interface */

	override fun executionInitialize(signalHandler: SignalHandler) {
		super.executionInitialize(signalHandler)
		isPowerOn = true
		signal = powerOnSignal
	}

	override fun executionStart(signalHandler: SignalHandler) {
		super.executionStart(signalHandler)
		requestActingAfter(signalHandler, 0, createActorData(null))
	}

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		bitWidth.write("bitWidth", writer)
		writer.writeString("logic", logic.customName)
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		bitWidth = BitWidth.read("bitWidth", reader)
		logic = Logic.withName(reader.readString("logic"))
	}
}