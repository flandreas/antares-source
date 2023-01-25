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
			incomingPortId: Int,
			currentVariableIndex: Int,
			equationSystem: LinearEquationSystem
		) {
			val row = DoubleArray(equationSystem.numberOfVariables) { 0.0 }

			val outgoingPortId = if (incomingPortId == 1) 2 else 1

			// If a voltage variable is -1 (not found), vertice is connected to ground
			val voltageVariableIncomingIndex = voltageNodes.indexOf(vertice.getPort<AnalogSignal>(incomingPortId).net!!.id)
			val voltageVariableOutgoingIndex = voltageNodes.indexOf(vertice.getPort<AnalogSignal>(outgoingPortId).net!!.id)

			if (voltageVariableIncomingIndex >= 0) {
				row[branches.size + voltageVariableIncomingIndex] = 1.0
			}
			if (voltageVariableOutgoingIndex >= 0) {
				row[branches.size + voltageVariableOutgoingIndex] = -1.0
			}
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
		incomingPortId: Int,
		currentVariableIndex: Int,
		equationSystem: LinearEquationSystem
	) {
		Companion.composeComponentConstituentEquation(
			this,
			voltageNodes, branches, incomingPortId, currentVariableIndex, equationSystem)
	}
}