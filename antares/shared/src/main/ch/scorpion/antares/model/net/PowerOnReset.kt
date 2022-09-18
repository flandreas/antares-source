package ch.scorpion.antares.model.net

import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.gate.AbstractDigitalGate
import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.BitWidthExpression
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.Actor
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.GraphElement
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

/**
 * A [Vertice] that outputs a fixed signal during circuit start-up, and then outputs the inverted signal
 * afterwards. Used for bringing a (typically sequential) circuit into the same defined state each time
 * the circuit is powered on.
 */
class PowerOnReset(
	bitWidth: BitWidth = BitWidth.BW_1,
	logic: Logic = Logic.POSITIVE
) : CalculatingVertice(CALCULATOR) {

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
					vertice.requestActingAfter(signalHandler, vertice.propagationDelay, vertice.createActorData(null))
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
		propagationDelay = 2 * AbstractDigitalGate.DEFAULT_PROPAGATION_DELAY
	}

	/** ---- [GraphElement] */

	override val type: String by lazy { Translations.getString("$BASE_RESOURCE_KEY.name") }

	override val typeDesc: String? get() = when (logic) {
		Logic.POSITIVE -> POS_TYPE_DESC
		Logic.NEGATIVE -> NEG_TYPE_DESC
	}

	override fun graphParamsChanged(graph: Graph) {
		(bitWidth as? BitWidthExpression)?.let { it.evaluateIn(graph)?.let { bw -> bitWidth = bw } }
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