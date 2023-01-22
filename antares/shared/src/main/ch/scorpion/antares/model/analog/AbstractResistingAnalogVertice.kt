package ch.scorpion.antares.model.analog

import ch.scorpion.antares.view.analog.AnalogCircuitBranch
import ch.scorpion.jabbah.base.math.LinearEquationSystem
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator

/** An [AnalogTwoPortVertice] with an electrical resistance. */
interface ResistingAnalogVertice : AnalogTwoPortVertice {
	val resistance: Double
}

/**
 * A base implementation of [ResistingAnalogVertice].
 */
abstract class AbstractResistingAnalogVertice<T: CalculatingVertice>(
	resistance: Double,
	calculator: VerticeCalculator<T>,
	baseResourceKey: String
) : AbstractAnalogTwoPortVertice<T>(calculator, baseResourceKey), ResistingAnalogVertice {

	companion object {

		/**
		 * Provided as static method to support implementations in other class hierarchies.
		 * Calculates V(X) - V(Y) - resistance * Current(i) = 0.
		 */
		fun composeComponentConstituentEquation(
			vertice: ResistingAnalogVertice,
			voltageNodes: List<Int>,
			branches: List<AnalogCircuitBranch>,
			currentVariableIndex: Int,
			equationSystem: LinearEquationSystem
		) {
			val row = DoubleArray(equationSystem.numberOfVariables) { 0.0 }

			val voltageVariableIndex1 = voltageNodes.indexOf(vertice.getPort<AnalogSignal>(1).portId)
			val voltageVariableIndex2 = voltageNodes.indexOf(vertice.getPort<AnalogSignal>(2).portId)

			row[branches.size + voltageVariableIndex1] = 1.0
			row[branches.size + voltageVariableIndex2] = -1.0
			row[currentVariableIndex] = -vertice.resistance

			equationSystem.addEquation(row, 0.0)
		}
	}

	override var resistance: Double = resistance
		set(value) {
			if (field != value) {
				field = value
				stateChanged(reason = MAIN_PROPERTY_STATE)
			}
		}

	override fun composeComponentConstituentEquation(
		voltageNodes: List<Int>,
		branches: List<AnalogCircuitBranch>,
		currentVariableIndex: Int,
		equationSystem: LinearEquationSystem
	) {
		Companion.composeComponentConstituentEquation(
			this,
			voltageNodes, branches, currentVariableIndex, equationSystem)
	}
}