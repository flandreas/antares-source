package ch.scorpion.antares.model.analog

import ch.scorpion.antares.view.analog.AnalogCircuitBranch
import ch.scorpion.antares.view.analog.AnalogElementMixin
import ch.scorpion.antares.view.analog.AnalogGraphView
import ch.scorpion.antares.view.analog.DynamicLinearEquationSystem
import ch.scorpion.antares.view.analog.falstad.FalstadAnalogCircuitAnalysis
import ch.scorpion.jabbah.graph.model.vertice.EmptyVerticeCalculator
import ch.scorpion.jabbah.io.StoreWriter
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader

class AnalogPower : AbstractAnalogVertice<AnalogPower>(
	EmptyVerticeCalculator,
	"library.element.AnalogPower",
	AnalogElementMixin(voltageSourceCount = 1, postCount = 1)
) {
	var voltage: Double = 5.0
		set(value) {
			if (value != field) {
				field = value
				stateChanged()
			}
		}

	init {
		addPort(AnalogPort())
	}

	/** ---- [Storable] */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeDouble("voltage", voltage)
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		voltage = reader.readDouble("voltage")
	}

	/** ---- [AnalogVertice] */

	override fun composeComponentConstituentEquation(
		circuitView: AnalogGraphView,
		voltageNodes: List<Int>,
		branches: List<AnalogCircuitBranch>,
		groundNodeNetId: Int,
		equationSystem: DynamicLinearEquationSystem
	) {
		val row = Array(equationSystem.variableCount) { DynamicLinearEquationSystem.ZERO }

		val voltageVariableIndex = voltageNodes.indexOf(getPort<AnalogSignal>().net!!.id)
		row[branches.size + voltageVariableIndex] = DynamicLinearEquationSystem.ONE

		equationSystem.addEquation(row) { voltage }
	}

	override fun stamp(analysis: FalstadAnalogCircuitAnalysis) {
		analysis.stampVoltageSource(0, analogElem.nodes[0], analogElem.voltageSource, voltage)
	}
}