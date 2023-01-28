package ch.scorpion.antares.model.analog

import ch.scorpion.antares.view.analog.AnalogCircuitBranch
import ch.scorpion.antares.view.analog.AnalogEdgeView
import ch.scorpion.antares.view.analog.DynamicLinearEquationSystem
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.graph.model.Net
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator

abstract class AbstractAnalogVertice<T: CalculatingVertice>(
	calculator: VerticeCalculator<T>,
	private val baseResourceKey: String
) : CalculatingVertice(calculator) {

	companion object {
		const val MAIN_PROPERTY_STATE = "mainPropertyState"
	}

	override val type: String get() = Translations.getString("${baseResourceKey}.name")

	override val typeDesc: String? get() = Translations.getOptionalString("${baseResourceKey}.desc")


	init {
		propagationDelay = 0
	}
}

/**
 * A [Vertice] with exactly two [AnalogPort]s.
 * Used as source of constituent equations when building the linear equation system during simulation.
 */
interface AnalogTwoPortVertice : Vertice {

	/**
	 * Composes the constituent equation for this [AnalogTwoPortVertice] during simulation.
	 *
	 * @param voltageNodes the [List] index identifies the voltage variable V(i), and the [List] value
	 * identifies the ID of the [Net] having that voltage
	 * @param branches the value at index i contains the [AnalogEdgeView] IDs for branch current variable I(i)
	 * @param incomingPortId the ID of the [AnalogPort] (starting with 1) at which the current comes into this [AnalogTwoPortVertice]
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
}