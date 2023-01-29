package ch.scorpion.antares.model.analog

import ch.scorpion.antares.view.analog.AnalogCircuitBranch
import ch.scorpion.antares.view.analog.AnalogGraphView
import ch.scorpion.antares.view.analog.DynamicLinearEquationSystem
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator

class AnalogGround : AbstractAnalogVertice<AnalogGround>(CALCULATOR, "library.element.AnalogGround") {

	companion object {
		private val CALCULATOR = Calculator()

		private class Calculator : VerticeCalculator<AnalogGround> {
			override fun calculate(vertice: AnalogGround, data: GraphActorData, signalHandler: SignalHandler) {
				// TODO
			}
		}
	}

	init {
		addPort(AnalogPort())
	}

	override fun composeComponentConstituentEquation(
		circuitView: AnalogGraphView,
		voltageNodes: List<Int>,
		branches: List<AnalogCircuitBranch>,
		groundNodeNetId: Int,
		equationSystem: DynamicLinearEquationSystem
	) {
		// empty, not used
	}
}