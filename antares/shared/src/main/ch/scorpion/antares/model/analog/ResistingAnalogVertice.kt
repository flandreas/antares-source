package ch.scorpion.antares.model.analog

import ch.scorpion.antares.view.analog.AnalogCircuitBranch
import ch.scorpion.antares.view.analog.AnalogElement
import ch.scorpion.antares.view.analog.DynamicLinearEquationSystem
import ch.scorpion.antares.view.analog.falstad.FalstadAnalogCircuitAnalysis
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator

/** An [AnalogVertice] with an electrical resistance. */
interface ResistingAnalogVertice : AnalogVertice {
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
			groundNodeNetId: Int,
			equationSystem: DynamicLinearEquationSystem
		) {
			val row = Array(equationSystem.variableCount) { DynamicLinearEquationSystem.ZERO }

			val outgoingPortId = if (incomingPortId == 1) 2 else 1

			// If a voltage variable is -1 (not found), vertice is connected to ground
			val voltageVariableIncomingIndex = voltageNodes.indexOf(vertice.getPort<AnalogSignal>(incomingPortId).net!!.id)
			val voltageVariableOutgoingIndex = voltageNodes.indexOf(vertice.getPort<AnalogSignal>(outgoingPortId).net!!.id)

			// (U2 - U1) - RI = 0
			if (voltageVariableIncomingIndex >= 0) {
				row[branches.size + voltageVariableIncomingIndex] = DynamicLinearEquationSystem.ONE
			}
			if (voltageVariableOutgoingIndex >= 0) {
				row[branches.size + voltageVariableOutgoingIndex] = DynamicLinearEquationSystem.MINUS_ONE
			}
			row[currentVariableIndex] = { -vertice.resistance }

			equationSystem.addEquation(row, DynamicLinearEquationSystem.ZERO)
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
		groundNodeNetId: Int,
		equationSystem: DynamicLinearEquationSystem
	) {
		Companion.composeComponentConstituentEquation(this,
			voltageNodes, branches, incomingPortId, currentVariableIndex, groundNodeNetId, equationSystem)
	}

	/** ---- [AnalogElement] */

	override fun stamp(analysis: FalstadAnalogCircuitAnalysis) {
		analysis.stampResistor(analogElem.nodes[0], analogElem.nodes[1], resistance)
	}
}