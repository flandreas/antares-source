package ch.scorpion.antares.model.analog

import ch.scorpion.antares.model.AntaresGraphTypes.Analog
import ch.scorpion.antares.model.AntaresGraphTypes.Digital
import ch.scorpion.antares.model.inout.AbstractCircuitInOut
import ch.scorpion.antares.model.inout.CircuitInOut
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.view.analog.AnalogCircuitBranch
import ch.scorpion.antares.view.analog.AnalogGraphView
import ch.scorpion.antares.view.analog.DynamicLinearEquationSystem
import ch.scorpion.antares.view.analog.DynamicLinearEquationSystem.Companion.ONE
import ch.scorpion.antares.view.analog.DynamicLinearEquationSystem.Companion.ZERO
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.ActorImpl
import ch.scorpion.jabbah.graph.model.*
import ch.scorpion.jabbah.graph.model.net.CombinedNet
import ch.scorpion.jabbah.graph.model.net.NetCombiner
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator
import ch.scorpion.jabbah.graph.view.GraphView

class AnalogCircuitInOut(
	name: String? = null,
	portType: PortType = PortType.INPUT
) : AbstractCircuitInOut<AnalogSignal>(
	port = AnalogPort(portType.reverse(), name),
	name = name,
	calculator = CALCULATOR
), AnalogVertice {

	companion object {
		private val HIGH_VOLTAGE = AnalogSignal.HIGH
		private val LOW_VOLTAGE = AnalogSignal.ZERO

		private val CALCULATOR = Calculator()

		private class Calculator : VerticeCalculator<AnalogCircuitInOut> {
			override fun calculate(vertice: AnalogCircuitInOut, data: GraphActorData, signalHandler: SignalHandler) {
				if (data.graphView is AnalogGraphView) {
					(data.graphView as AnalogGraphView).requestActing(signalHandler)
				}
			}
		}
	}

	/** ---- [GraphPort] */

	override var signal: AnalogSignal? = LOW_VOLTAGE
		set(value) {
			if (signal != value) {
				field = value
				stateChanged()
			}
		}

	/** ---- [GraphInput] */

	override fun setIncomingSignal(signal: AnalogSignal?, signalHandler: SignalHandler, force: Boolean) {
		this.signal = signal
		BaseModule.eventBus.post(AnalogCalculationRequest(this, signalHandler))
	}

	/** ---- [NetCombiner] */

	override val isNetCombiner: Boolean get() = false

	override fun requiresCombinedNets(signalHandler: SignalHandler): Boolean = false

	override fun <T : Any> createCombinedNetsFor(
		outputPort: OutputPort<T>,
		inputPort: InputPort<T>,
		signalHandler: SignalHandler
	): Collection<CombinedNet<T>> = emptyList()

	/** ---- [CircuitInOut] */

	override fun setSignalManually(signal: AnalogSignal, signalHandler: SignalHandler, graphView: GraphView?) {
		setIncomingSignal(signal, signalHandler)
	}

	override fun inputChanged(input: InputPort<*>, signalHandler: SignalHandler, force: Boolean) {
		if (portType.isOutput) {
			signal = input.net!!.signal as AnalogSignal?
		}
	}

	/** ---- [AnalogVertice] */

	override fun composeComponentConstituentEquation(
		circuitView: AnalogGraphView,
		voltageNodes: List<Int>,
		branches: List<AnalogCircuitBranch>,
		groundNodeNetId: Int,
		equationSystem: DynamicLinearEquationSystem
	) {
		val row = Array(equationSystem.variableCount) { ZERO }

		if (getPort<AnalogSignal>().portType.isOutput) {
			// Input: Constant voltage
			val voltageVariableIndex = voltageNodes.indexOf(getPort<AnalogSignal>().net!!.id)
			row[branches.size + voltageVariableIndex] = ONE
			// TODO Handle undefined differently
			equationSystem.addEquation(row) { signal?.voltage ?: AnalogSignal.ZERO.voltage }
		} else {
			// Output: No electrical current flowing outwards
			val currentVariableIndex = AnalogCircuitBranch.getCurrentVariableIndex(circuitView, this, branches, 1)
			row[currentVariableIndex] = ONE
			equationSystem.addEquation(row, ZERO)
		}
	}

	override fun handleAnalogPortChanged(port: AnalogPort, signalHandler: SignalHandler) {
		if (portType.isOutput) {
			setOutgoingSignal(getPort<AnalogSignal>().net!!.signal!!, signalHandler)
		}
	}

	/** ---- [ActorImpl] */

	override fun executionStart(signalHandler: SignalHandler) {
		super.executionStart(signalHandler)
		signal = LOW_VOLTAGE
	}

	/** ---- [AnalogCircuitInOut] */

	fun toggle(signalHandler: SignalHandler, graphView: GraphView) {
		signal = if (signal == HIGH_VOLTAGE) LOW_VOLTAGE else HIGH_VOLTAGE
		requestActingAfter(signalHandler, propagationDelay, createActorData(null, graphView = graphView))
	}

	private fun setOutgoingSignal(signal: AnalogSignal, signalHandler: SignalHandler) {
		this.signal = signal
		if (portType.isOutput) {
			propagateToSubGraphOutputPort(signal, signalHandler)
		}
	}

	private fun propagateToSubGraphOutputPort(signal: AnalogSignal, signalHandler: SignalHandler) {
		val outgoingSignal = Digital.adaptTo<AnalogSignal, DigitalSignal>(Analog).convertOutgoingSignal(signal)!!
		(subGraphOutputPort as SubGraphOutputPort<DigitalSignal>?)?.propagateSignal(outgoingSignal, signalHandler)
		subGraphOutputPort?.flush(signalHandler, false)
	}
}