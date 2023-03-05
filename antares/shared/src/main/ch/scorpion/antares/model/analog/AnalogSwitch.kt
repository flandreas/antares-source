package ch.scorpion.antares.model.analog

import ch.scorpion.antares.model.input.AbstractSwitch
import ch.scorpion.antares.view.analog.AnalogCircuitBranch
import ch.scorpion.antares.view.analog.AnalogGraphView
import ch.scorpion.antares.view.analog.DynamicLinearEquationSystem
import ch.scorpion.antares.view.module.AntaresViewModule
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.GraphActorData

/**
 * The electrical [resistance] of [AnalogSwitch] depends on the state of [isOn].
 */
class AnalogSwitch : AbstractSwitch<AnalogSwitch>(CALCULATOR), ResistingAnalogVertice, AnalogTwoPortVertice {

	companion object {
		private const val BASE_RESOURCE_KEY = "library.element.AnalogSwitch"

		private val CALCULATOR = Calculator()

		private class Calculator : AbstractSwitch.Companion.AbstractSwitchCalculator<AnalogSwitch>() {
			override fun calculate(vertice: AnalogSwitch, data: GraphActorData, signalHandler: SignalHandler) {
				super.calculate(vertice, data, signalHandler)

				if (data.graphView is AnalogGraphView) {
					AntaresViewModule.analogCircuitCalculator.calculate(
						(data.graphView as AnalogGraphView).ensureAnalysis(),
						signalHandler
					)
				}
			}
		}
	}

	override val type: String get() = Translations.getString("${BASE_RESOURCE_KEY}.name")

	override val typeDesc: String? get() = Translations.getOptionalString("${BASE_RESOURCE_KEY}.desc")

	override val resistance: Double get() = if (isOn) 0.0 else 100_000_000.0

	init {
		addPort(AnalogPort())
		addPort(AnalogPort())
		propagationDelay = 0
	}

	/** ---- AnalogTwoPortVertice */

	override fun composeComponentConstituentEquation(
		voltageNodes: List<Int>,
		branches: List<AnalogCircuitBranch>,
		incomingPortId: Int,
		currentVariableIndex: Int,
		groundNodeNetId: Int,
		equationSystem: DynamicLinearEquationSystem
	) {
		AbstractResistingAnalogVertice.composeComponentConstituentEquation(
			this,
			voltageNodes, branches, incomingPortId, currentVariableIndex, groundNodeNetId, equationSystem)
	}

	override fun composeComponentConstituentEquation(
		circuitView: AnalogGraphView,
		voltageNodes: List<Int>,
		branches: List<AnalogCircuitBranch>,
		groundNodeNetId: Int,
		equationSystem: DynamicLinearEquationSystem
	) {
		AbstractAnalogTwoPortVertice.composeComponentConstituentEquation(this,
			circuitView, voltageNodes, branches, groundNodeNetId, equationSystem)
	}
}