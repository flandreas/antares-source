package ch.scorpion.antares.model.analog

import ch.scorpion.antares.model.analog.AnalogTwoPortVertice.Companion.incomingCurrentPortId
import ch.scorpion.antares.view.analog.AnalogCircuitBranch
import ch.scorpion.antares.view.analog.AnalogEdgeView
import ch.scorpion.antares.view.analog.AnalogGraphView
import ch.scorpion.antares.view.analog.DynamicLinearEquationSystem
import ch.scorpion.jabbah.graph.model.Net
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator

/**
 * An [AnalogVertice] with exactly two [AnalogPort]s.
 */
interface AnalogTwoPortVertice : AnalogVertice {

	companion object {

		/**
		 * Returns the [Port] of [vertice] at which the electrical current flows into [vertice]
		 */
		fun incomingCurrentPortId(
			circuitView: AnalogGraphView,
			vertice: AnalogVertice,
			branch: AnalogCircuitBranch,
			port1Id: Int = 1,
			port2Id: Int = 2
		): Int {
			val port1 = vertice.getPort<AnalogSignal>(port1Id)
			val port2 = vertice.getPort<AnalogSignal>(port2Id)
			val edgeView1 = circuitView.getEdgeView(port1)!!
			return if (branch.isPositive(edgeView1.id)) {
				if (edgeView1.destination!!.port === port1) {
					port1.portId
				} else {
					port2.portId
				}
			} else {
				if (edgeView1.destination!!.port === port1) {
					port2.portId
				} else {
					port1.portId
				}
			}
		}
	}

	/**
	 * Composes the constituent equation for this [AbstractAnalogTwoPortVertice] during simulation.
	 *
	 * @param voltageNodes the [List] index identifies the voltage variable V(i), and the [List] value
	 * identifies the ID of the [Net] having that voltage
	 * @param branches the value at index i contains the [AnalogEdgeView] IDs for branch current variable I(i)
	 * @param incomingPortId the ID of the [AnalogPort] (starting with 1) at which the current comes into this [AnalogVertice]
	 * @param currentVariableIndex the index in [branches] representing the [AnalogCircuitBranch] with the incoming current
	 * @param groundNodeNetId the ID of the ground [Net]
	 * @param equationSystem the [DynamicLinearEquationSystem] to add the composed equation to
	 */
	fun composeComponentConstituentEquation(
		voltageNodes: List<Int>,
		branches: List<AnalogCircuitBranch>,
		incomingPortId: Int,
		currentVariableIndex: Int,
		groundNodeNetId: Int,
		equationSystem: DynamicLinearEquationSystem
	)
}
abstract class AbstractAnalogTwoPortVertice<T: CalculatingVertice>(
	calculator: VerticeCalculator<T>,
	baseResourceKey: String
) : AbstractAnalogVertice<T>(calculator, baseResourceKey), AnalogTwoPortVertice {

	init {
		addPort(AnalogPort())
		addPort(AnalogPort())
	}

	companion object {

		fun composeComponentConstituentEquation(
			vertice: AnalogTwoPortVertice,
			circuitView: AnalogGraphView,
			voltageNodes: List<Int>,
			branches: List<AnalogCircuitBranch>,
			groundNodeNetId: Int,
			equationSystem: DynamicLinearEquationSystem
		) {
			val currentVariableIndex = AnalogCircuitBranch.getCurrentVariableIndex(circuitView, vertice, branches)
			val branch = branches[currentVariableIndex]
			val incomingPortId = incomingCurrentPortId(circuitView, vertice, branch)
			vertice.composeComponentConstituentEquation(
				voltageNodes, branches, incomingPortId, currentVariableIndex, groundNodeNetId, equationSystem
			)
		}
	}

	override fun composeComponentConstituentEquation(
		circuitView: AnalogGraphView,
		voltageNodes: List<Int>,
		branches: List<AnalogCircuitBranch>,
		groundNodeNetId: Int,
		equationSystem: DynamicLinearEquationSystem
	) {
		Companion.composeComponentConstituentEquation(this,
			circuitView, voltageNodes, branches, groundNodeNetId, equationSystem)
	}
}