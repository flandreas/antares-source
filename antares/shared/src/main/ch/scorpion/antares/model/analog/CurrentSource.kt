package ch.scorpion.antares.model.analog

import ch.scorpion.antares.view.analog.AnalogCircuitBranch
import ch.scorpion.antares.view.analog.DynamicLinearEquationSystem
import ch.scorpion.jabbah.graph.model.vertice.EmptyVerticeCalculator
import ch.scorpion.jabbah.graph.model.OutputPort
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

/**
 * Produces [current] outgoing at [OutputPort] 1.
 */
class CurrentSource(
	current: Double = DEF_CURRENT
) : AbstractAnalogTwoPortVertice<CurrentSource>(EmptyVerticeCalculator, "library.element.CurrentSource") {

	companion object {
		private const val DEF_CURRENT = 0.1
	}

	var current: Double = current
		set(value) {
			if (field != value) {
				field = value
				stateChanged(reason = MAIN_PROPERTY_STATE)
			}
		}

	/** ---- [Storable] interface */

	override fun read(reader: StoreReader) {
		super.read(reader)
		current = reader.readDouble("current")
	}

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeDouble("current", current)
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
		val row = Array(equationSystem.variableCount) { DynamicLinearEquationSystem.ZERO }
		row[currentVariableIndex] = DynamicLinearEquationSystem.ONE
		equationSystem.addEquation(row) { current }
	}
}