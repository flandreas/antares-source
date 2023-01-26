package ch.scorpion.antares.model.analog

import ch.scorpion.antares.view.analog.AnalogCircuitBranch
import ch.scorpion.antares.view.analog.DynamicLinearEquationSystem
import ch.scorpion.jabbah.base.Translations
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

interface AnalogTwoPortVertice : Vertice {
	fun composeComponentConstituentEquation(
		voltageNodes: List<Int>,
		branches: List<AnalogCircuitBranch>,
		incomingPortId: Int,
		currentVariableIndex: Int,
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