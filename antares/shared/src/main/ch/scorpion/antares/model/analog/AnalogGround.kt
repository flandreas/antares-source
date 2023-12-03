package ch.scorpion.antares.model.analog

import ch.scorpion.antares.view.analog.AnalogCircuitBranch
import ch.scorpion.antares.view.analog.AnalogElementMixin
import ch.scorpion.antares.view.analog.AnalogGraphView
import ch.scorpion.antares.view.analog.DynamicLinearEquationSystem
import ch.scorpion.antares.view.analog.falstad.FalstadAnalogCircuitAnalysis
import ch.scorpion.jabbah.graph.model.vertice.EmptyVerticeCalculator

class AnalogGround : AbstractAnalogVertice<AnalogGround>(
	EmptyVerticeCalculator,
	"library.element.AnalogGround",
	AnalogElementMixin(voltageSourceCount = 1, postCount = 1)
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

	override fun stamp(analysis: FalstadAnalogCircuitAnalysis) {
		analysis.stampVoltageSource(0, analogElem.nodes[0], analogElem.voltageSource, 0.0)
	}
}