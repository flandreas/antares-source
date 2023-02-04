package ch.scorpion.antares.model.analog

import ch.scorpion.antares.model.inout.CircuitInOut
import ch.scorpion.antares.view.analog.AnalogCircuitBranch
import ch.scorpion.antares.view.analog.AnalogGraphView
import ch.scorpion.antares.view.analog.DynamicLinearEquationSystem
import ch.scorpion.antares.view.analog.DynamicLinearEquationSystem.Companion.ONE
import ch.scorpion.antares.view.module.AntaresViewModule
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.*
import ch.scorpion.jabbah.graph.model.net.CombinedNet
import ch.scorpion.jabbah.graph.model.net.NetCombiner
import ch.scorpion.jabbah.graph.model.vertice.AbstractGraphPort
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator
import ch.scorpion.jabbah.graph.view.GraphView

class AnalogCircuitInOut(
	name: String? = null
) : AbstractGraphPort<AnalogSignal>(
	port = AnalogPort(name),
	name = name,
	calculator = CALCULATOR
), CircuitInOut<AnalogSignal>, AnalogVertice {

	companion object {

		private const val BASE_RESOURCE_KEY = "library.element.GraphInOut"

		private val CALCULATOR = Calculator()

		private val HIGH_VOLTAGE = AnalogSignal(5.0)
		private val LOW_VOLTAGE = AnalogSignal.ZERO

		private class Calculator : VerticeCalculator<AnalogCircuitInOut> {
			override fun calculate(vertice: AnalogCircuitInOut, data: GraphActorData, signalHandler: SignalHandler) {
				if (data.graphView is AnalogGraphView) {
					AntaresViewModule.analogCircuitCalculator.calculate((data.graphView as AnalogGraphView).analysis, signalHandler)
				}
			}
		}
	}

	override val type: String get() = Translations.getString("$BASE_RESOURCE_KEY.name")

	override val typeDesc: String? get() = Translations.getOptionalString("$BASE_RESOURCE_KEY.desc")

	/** ---- [GraphPort] */

	override var signal: AnalogSignal = HIGH_VOLTAGE
		set(value) {
			if (signal != value) {
				field = value
				stateChanged()
			}
		}

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

	override fun requiresCombinedNets(signalHandler: SignalHandler): Boolean = false

	override fun <T : Any> createCombinedNetsFor(
		outputPort: OutputPort<T>,
		inputPort: InputPort<T>,
		signalHandler: SignalHandler
	): Collection<CombinedNet<T>> = emptyList()

	/** ---- [CircuitInOut] */

	override val isToplevel: Boolean get() = true

	override fun setSignalManually(signal: AnalogSignal, signalHandler: SignalHandler) {
		throw UnsupportedOperationException("not implemented")
	}

	/** ---- [AnalogVertice] */

	override fun composeComponentConstituentEquation(
		circuitView: AnalogGraphView,
		voltageNodes: List<Int>,
		branches: List<AnalogCircuitBranch>,
		groundNodeNetId: Int,
		equationSystem: DynamicLinearEquationSystem
	) {
		val row = Array(equationSystem.variableCount) { DynamicLinearEquationSystem.ZERO }

		val voltageVariableIndex = voltageNodes.indexOf(getPort<AnalogSignal>().net!!.id)
		row[branches.size + voltageVariableIndex] = ONE

		equationSystem.addEquation(row) { signal.voltage }
	}

	fun toggle(signalHandler: SignalHandler, graphView: GraphView) {
		signal = if (signal == HIGH_VOLTAGE) LOW_VOLTAGE else HIGH_VOLTAGE
		requestActingAfter(signalHandler, propagationDelay, createActorData(null, graphView = graphView))
	}
}