package ch.scorpion.antares.model.analog

import ch.scorpion.antares.model.AntaresGraphTypes
import ch.scorpion.antares.view.analog.*
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
import ch.scorpion.jabbah.graph.model.oscilloscope.OscilloscopeProbeVertice
import ch.scorpion.jabbah.graph.model.port.PortFactory

class AnalogOscilloscopeProbeVertice(
	portFactory: PortFactory = GraphModelModule.portFactory,
	private val analogElement: AnalogElementMixin = AnalogElementMixin()
) : OscilloscopeProbeVertice<AnalogSignal>(AntaresGraphTypes.Analog, portFactory),
	AnalogVertice,
	AnalogElement by analogElement
{
	override fun composeComponentConstituentEquation(
		circuitView: AnalogGraphView,
		voltageNodes: List<Int>,
		branches: List<AnalogCircuitBranch>,
		groundNodeNetId: Int,
		equationSystem: DynamicLinearEquationSystem
	) {
		// empty
	}

	override fun handleAnalogPortChanged(port: AnalogPort, signalHandler: SignalHandler) {
		stateChanged(signalHandler)
	}
}