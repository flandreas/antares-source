package io.antarescircuit.antares.model.net

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
import io.antarescircuit.jabbah.graph.model.PortType
import io.antarescircuit.jabbah.graph.model.vertice.CalculatingVertice
import io.antarescircuit.jabbah.graph.model.vertice.VerticeCalculator
import io.antarescircuit.jabbah.io.Storable
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter

class Power(
	bitWidth: BitWidth = BitWidth.BW_1
) : CalculatingVertice(CALCULATOR), AdjustableBitWidth {

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
		propagationDelay = LongValueImpl.ONE
	}

	/** ---- [AdjustableBitWidth] */

	override fun adjustBitWidth(portId: Int, bitWidth: BitWidth): Boolean {
		this.bitWidth = bitWidth
		return true
	}

	/** ---- [GraphElement] */

	override fun graphParamsChanged(graph: Graph) {
		super.graphParamsChanged(graph)
		(bitWidth as? BitWidthExpression)?.let { it.evaluateIn(graph)?.let { bw -> bitWidth = bw } }
	}

	/** ---- [Actor] */

	override fun executionInitialize(signalHandler: SignalHandler) {
		super.executionInitialize(signalHandler)
		getOutput<DigitalSignal>().setOutgoingSignalBuffered(DigitalSignalFactory.trueValue(bitWidth), signalHandler)
	}

	override fun executionStart(signalHandler: SignalHandler) {
		super.executionStart(signalHandler)
		requestActingAfter(signalHandler, propagationDelay.value, createActorData(null))
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