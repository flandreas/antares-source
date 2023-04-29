package ch.scorpion.antares.model.analog

import ch.scorpion.antares.model.net.TransistorIF
import ch.scorpion.antares.model.net.TransistorIF.Companion.DEFAULT_TRANSISTOR_TYPE
import ch.scorpion.antares.model.net.TransistorType
import ch.scorpion.antares.view.analog.AnalogCircuitBranch
import ch.scorpion.antares.view.analog.AnalogGraphView
import ch.scorpion.antares.view.analog.DynamicLinearEquationSystem
import ch.scorpion.antares.view.analog.DynamicLinearEquationSystem.Companion.MINUS_ONE
import ch.scorpion.antares.view.analog.DynamicLinearEquationSystem.Companion.ONE
import ch.scorpion.antares.view.analog.DynamicLinearEquationSystem.Companion.ZERO
import ch.scorpion.jabbah.graph.model.vertice.EmptyVerticeCalculator
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

/**
 * [AnalogTransistor] acts during simulation as a "Voltage Controlled Current Source (VCC)".
 * See https://ultimateelectronicsbook.com/dependent-sources/.
 */
class AnalogTransistor(
	transistorType: TransistorType = DEFAULT_TRANSISTOR_TYPE,
	var gain: Double = DEF_GAIN
) : AbstractAnalogVertice<AnalogTransistor>(
	EmptyVerticeCalculator,
	"library.element.AnalogTransistor"
), TransistorIF<AnalogSignal> {

	companion object {
		private const val DEF_GAIN = 0.1
	}

	override val type: String get() = super<TransistorIF>.type

	override val typeDesc: String? get() = super<TransistorIF>.typeDesc

	override var transistorType: TransistorType = transistorType
		set(value) {
			if (field != value) {
				field = value
				stateChanged()
			}
		}

	override val isOn: Boolean get() = false

	init {
		addPort(AnalogPort(name = "S"))
		addPort(AnalogPort(name = "G"))
		addPort(AnalogPort(name = "D"))
	}

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super<TransistorIF>.write(writer)
		writer.writeDouble("gain", gain)
	}

	override fun read(reader: StoreReader) {
		super<TransistorIF>.read(reader)
		gain = reader.readDouble("gain")
	}

	override fun composeComponentConstituentEquation(
		circuitView: AnalogGraphView,
		voltageNodes: List<Int>,
		branches: List<AnalogCircuitBranch>,
		groundNodeNetId: Int,
		equationSystem: DynamicLinearEquationSystem
	) {
		composeDrainSourceEquation(circuitView, voltageNodes, branches, equationSystem)
		composeGateEquation(circuitView, branches, equationSystem)
	}

	private fun composeDrainSourceEquation(
		circuitView: AnalogGraphView,
		voltageNodes: List<Int>,
		branches: List<AnalogCircuitBranch>,
		equationSystem: DynamicLinearEquationSystem
	) {
		val row = Array(equationSystem.variableCount) { ZERO }

		val sourceCurrentIndex = AnalogCircuitBranch.getCurrentVariableIndex(circuitView, this, branches)
		if (AnalogCircuitBranch.isCurrentPositive(circuitView, this, branches)) {
			row[sourceCurrentIndex] = MINUS_ONE
		} else {
			row[sourceCurrentIndex] = ONE
		}

		// If a voltage variable is -1 (not found), vertice is connected to ground
		val gateVoltageIndex = voltageNodes.indexOf(gatePort.net!!.id)
		val sourceVoltageIndex = voltageNodes.indexOf(sourcePort.net!!.id)

		when (transistorType) {
			TransistorType.N -> {
				// I = k(Ug - Us)
				if (gateVoltageIndex >= 0) {
					row[branches.size + gateVoltageIndex] = { -gain }
				}
				if (sourceVoltageIndex >= 0) {
					row[branches.size + sourceVoltageIndex] = { gain }
				}
				equationSystem.addEquation(row, ZERO)
			}
			TransistorType.P -> {
				// I = I0 - k(Ug - Us) with I0 = gain * Umax
				if (gateVoltageIndex >= 0) {
					row[branches.size + gateVoltageIndex] = { gain }
				}
				if (sourceVoltageIndex >= 0) {
					row[branches.size + sourceVoltageIndex] = { -gain }
				}
				equationSystem.addEquation(row) { gain * AnalogSignal.HIGH.voltage }
			}
		}
	}

	private fun composeGateEquation(
		circuitView: AnalogGraphView,
		branches: List<AnalogCircuitBranch>,
		equationSystem: DynamicLinearEquationSystem
	) {
		val row = Array(equationSystem.variableCount) { ZERO }

		/** Gate-Source current must be 0. */
		val currentVariableIndex = AnalogCircuitBranch.getCurrentVariableIndex(circuitView, this, branches, gatePort.portId)
		row[currentVariableIndex] = ONE

		equationSystem.addEquation(row, ZERO)
	}
}