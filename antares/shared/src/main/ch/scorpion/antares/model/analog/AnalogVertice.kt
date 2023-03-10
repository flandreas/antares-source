package ch.scorpion.antares.model.analog

import ch.scorpion.antares.view.analog.AnalogCircuitBranch
import ch.scorpion.antares.view.analog.AnalogEdgeView
import ch.scorpion.antares.view.analog.AnalogGraphView
import ch.scorpion.antares.view.analog.DynamicLinearEquationSystem
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.Net
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator

/**
 * Used as source of constituent equations when building the linear equation system during simulation.
 */
interface AnalogVertice : Vertice {

	/**
	 * Composes the constituent equation for this [AnalogVertice] during simulation.
	 *
	 * @param voltageNodes the [List] index identifies the voltage variable V(i), and the [List] value
	 * identifies the ID of the [Net] having that voltage
	 * @param branches the value at index i contains the [AnalogEdgeView] IDs for branch current variable I(i)
	 * @param groundNodeNetId the ID of the ground [Net]
	 * @param equationSystem the [DynamicLinearEquationSystem] to add the composed equation to
	 */
	fun composeComponentConstituentEquation(
		circuitView: AnalogGraphView,
		voltageNodes: List<Int>,
		branches: List<AnalogCircuitBranch>,
		groundNodeNetId: Int,
		equationSystem: DynamicLinearEquationSystem
	)

	fun handleAnalogPortChanged(port: AnalogPort, signalHandler: SignalHandler) {
		// empty
	}
}

abstract class AbstractAnalogVertice<T: CalculatingVertice>(
	calculator: VerticeCalculator<T>,
	val baseResourceKey: String
) : CalculatingVertice(calculator), AnalogVertice {

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
 * Posted by [AnalogVertice] on the system's [EventBus] to indicate that an
 * [AnalogGraphView] containing [source] should recalculate. Needed because
 * some [AnalogVertice] are triggered by low-level model methods and don't
 * have access to [AnalogGraphView] at that moment.
 */
data class AnalogCalculationRequest(
	val source: AnalogVertice,
	val signalHandler: SignalHandler
)

