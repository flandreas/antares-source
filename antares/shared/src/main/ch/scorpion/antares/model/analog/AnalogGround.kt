package ch.scorpion.antares.model.analog

import ch.scorpion.antares.view.analog.AnalogCircuitBranch
import ch.scorpion.antares.view.analog.AnalogGraphView
import ch.scorpion.antares.view.analog.DynamicLinearEquationSystem
import ch.scorpion.jabbah.graph.model.vertice.EmptyVerticeCalculator

class AnalogGround : AbstractAnalogVertice<AnalogGround>(
	EmptyVerticeCalculator,
	"library.element.AnalogGround"
) {

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