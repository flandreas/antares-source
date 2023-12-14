package ch.scorpion.antares.model.analog

import ch.scorpion.antares.view.analog.AnalogCircuitBranch
import ch.scorpion.antares.view.analog.AnalogElement
import ch.scorpion.antares.view.analog.DynamicLinearEquationSystem
import ch.scorpion.antares.view.analog.DynamicLinearEquationSystem.Companion.MINUS_ONE
import ch.scorpion.antares.view.analog.DynamicLinearEquationSystem.Companion.ONE
import ch.scorpion.antares.view.analog.DynamicLinearEquationSystem.Companion.ZERO
import ch.scorpion.antares.view.analog.falstad.FalstadAnalogCircuitAnalysis
import ch.scorpion.jabbah.graph.model.vertice.EmptyVerticeCalculator
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

/**
 * Port 1 is the plus pin, port 2 is the minus pin.
 */
class Battery(
	voltage: Double = DEF_VOLTAGE
) : AbstractAnalogTwoPortVertice<Battery>(
	EmptyVerticeCalculator,
	"library.element.Battery"
) {

	companion object {
		private const val DEF_VOLTAGE = 5.0
	}

	/** The constant voltage (in V) this [Battery] produces. */
	var voltage: Double = voltage
		set(value) {
			if (field != value) {
				field = value
				stateChanged(reason = MAIN_PROPERTY_STATE)
			}
		}

	val positivePort: AnalogPort get() = getPort<AnalogPort>(1) as AnalogPort
	val negativePort: AnalogPort get() = getPort<AnalogPort>(2) as AnalogPort

	/** ---- [Storable] interface */

	override fun read(reader: StoreReader) {
		super.read(reader)
		voltage = reader.readDouble("voltage")
	}

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeDouble("voltage", voltage)
	}

	/** ---- [AnalogTwoPortVertice] */

	/** Constant voltage at positive port. */
	override fun composeComponentConstituentEquation(
		voltageNodes: List<Int>,
		branches: List<AnalogCircuitBranch>,
		incomingPortId: Int,
		currentVariableIndex: Int,
		groundNodeNetId: Int,
		equationSystem: DynamicLinearEquationSystem
	) {
		val row = Array(equationSystem.variableCount) { ZERO }

		val posVoltageVariableIndex = voltageNodes.indexOf(positivePort.net!!.id)
		val negVoltageVariableIndex = voltageNodes.indexOf(negativePort.net!!.id)

		row[branches.size + posVoltageVariableIndex] = MINUS_ONE
		if (negVoltageVariableIndex >= 0) {
			row[branches.size + negVoltageVariableIndex] = ONE
		}
		equationSystem.addEquation(row) { -voltage }
	}

	override val voltageSourceCount: Int get() = 1

	/** ---- [AnalogElement] */

	override fun stamp(analysis: FalstadAnalogCircuitAnalysis) {
		analysis.stampVoltageSource(analogElem.nodes[1], analogElem.nodes[0], analogElem.voltageSource, voltage)
	}
}