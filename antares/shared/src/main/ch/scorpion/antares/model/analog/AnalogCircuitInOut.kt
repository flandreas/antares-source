package ch.scorpion.antares.model.analog

import ch.scorpion.antares.model.inout.CircuitInOut
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.*
import ch.scorpion.jabbah.graph.model.net.CombinedNet
import ch.scorpion.jabbah.graph.model.net.NetCombiner
import ch.scorpion.jabbah.graph.model.vertice.AbstractGraphPort
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator

class AnalogCircuitInOut(
	name: String? = null
) : AbstractGraphPort<AnalogSignal>(
	port = AnalogPort(name),
	name = name,
	calculator = CALCULATOR
), CircuitInOut<AnalogSignal> {

	companion object {
		private const val BASE_RESOURCE_KEY = "library.element.GraphInOut"

		private val CALCULATOR = Calculator()

		private class Calculator : VerticeCalculator<AnalogCircuitInOut> {
			override fun calculate(vertice: AnalogCircuitInOut, data: GraphActorData, signalHandler: SignalHandler) {
				throw UnsupportedOperationException("not implemented")
			}
		}
	}

	override val type: String get() = Translations.getString("$BASE_RESOURCE_KEY.name")

	override val typeDesc: String? get() = Translations.getOptionalString("$BASE_RESOURCE_KEY.desc")

	/** ---- [GraphPort] */

	override var signal: AnalogSignal = AnalogSignal.ZERO

	override var portType: PortType = PortType.INOUT
		set(@Suppress("UNUSED_PARAMETER") value) { throw UnsupportedOperationException() }

	/** ---- [GraphInput] */

	override var subGraphInputPort: SubGraphInputPort<AnalogSignal>? = null

	override fun setIncomingSignal(signal: AnalogSignal?, signalHandler: SignalHandler, force: Boolean) {
		throw UnsupportedOperationException("not implemented")
	}

	/** ---- [GraphOutput] */

	override var subGraphOutputPort: SubGraphOutputPort<AnalogSignal>? = null

	/** ---- [NetCombiner] */

	override fun requiresCombinedNets(signalHandler: SignalHandler): Boolean {
		throw UnsupportedOperationException("not implemented")
	}

	override fun <T : Any> createCombinedNetsFor(
		outputPort: OutputPort<T>,
		inputPort: InputPort<T>,
		signalHandler: SignalHandler
	): Collection<CombinedNet<T>> {
		throw UnsupportedOperationException("not implemented")
	}

	/** ---- [CircuitInOut] */

	override val isToplevel: Boolean get() = true

	override fun setSignalManually(signal: AnalogSignal, signalHandler: SignalHandler) {
		throw UnsupportedOperationException("not implemented")
	}
}