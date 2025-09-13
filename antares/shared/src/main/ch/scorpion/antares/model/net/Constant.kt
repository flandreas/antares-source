package ch.scorpion.antares.model.net

import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.*
import ch.scorpion.antares.model.vertice.AdjustableBitWidth
import ch.scorpion.jabbah.base.LongValue
import ch.scorpion.jabbah.base.LongValueImpl
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.Actor
import ch.scorpion.jabbah.graph.model.*
import ch.scorpion.jabbah.graph.model.param.LongValueExpression
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

/**
 * A [Vertice] that produces a configurable constant [DigitalSignal] at its single output.
 */
class Constant(
	value: LongValue = LongValueImpl(0L)
) : CalculatingVertice(CALCULATOR), AdjustableBitWidth {

	init {
		addPort(DigitalPortImpl(portType = PortType.OUTPUT, bitWidth = BitWidth.smallest(value.value.toULong()) ?: BitWidth.BW_1))
		propagationDelay = LongValueImpl.ONE
	}

	companion object {

		private const val BASE_RESOURCE_KEY = "library.element.Constant"
		private val TYPE get() = Translations.getString("$BASE_RESOURCE_KEY.name")
		private val TYPE_DESC get() = Translations.getOptionalString("$BASE_RESOURCE_KEY.desc")

		private val CALCULATOR = Calculator()

		private class Calculator : VerticeCalculator<Constant> {
			override fun calculate(vertice: Constant, data: GraphActorData, signalHandler: SignalHandler) {
				vertice.getOutput<DigitalSignal>().setOutgoingSignal(vertice.valueSignal, signalHandler)
			}
		}

		private fun toDigitalSignal(value: LongValue, bitWidth: BitWidth): DigitalSignal =
            DigitalSignalFactory.of(bitWidth, value.value.toULong())
	}

	override val type: String get() = TYPE
	override val typeDesc: String? get() = TYPE_DESC

	var valueSignal: DigitalSignal = toDigitalSignal(value, BitWidth.smallest(value.value.toULong()) ?: BitWidth.BW_1)
		private set

	var value: LongValue = value
		set(value) {
			field = value
			valueSignal = toDigitalSignal(value, bitWidth)
			stateChanged()
		}


	var bitWidth: BitWidth
		get() = (getOutput<DigitalSignal>() as DigitalPort).bitWidth
		set(newValue) {
			if (newValue != bitWidth) {
				(getOutput<DigitalSignal>() as DigitalPort).bitWidth = newValue
				valueSignal = DigitalSignalFactory.of(bitWidth, value.value.toULong())
				stateChanged()
			}
		}

	/** ---- [GraphElement] */

	override fun graphParamsChanged(graph: Graph) {
		super.graphParamsChanged(graph)
		(bitWidth as? BitWidthExpression)?.let { it.evaluateIn(graph)?.let { bw -> bitWidth = bw } }
		(value as? LongValueExpression)?.let {
			it.evaluateIn(graph)?.let { v -> value = v  }
		}
	}

	/** ---- [Actor] */

	override fun executionStart(signalHandler: SignalHandler) {
		super.executionStart(signalHandler)
		requestActingAfter(signalHandler, propagationDelay.value, createActorData(null))
	}

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		bitWidth.write("bitWidth", writer)
		LongValueExpression.write("value", value, writer)
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		bitWidth = BitWidth.read("bitWidth", reader)
		value = LongValueExpression.read("value", reader)
	}

	/** ---- [AdjustableBitWidth] */

	override fun adjustBitWidth(portId: Int, bitWidth: BitWidth): Boolean {
		this.bitWidth = bitWidth
		return true
	}
}