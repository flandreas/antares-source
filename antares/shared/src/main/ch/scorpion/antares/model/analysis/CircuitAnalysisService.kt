package ch.scorpion.antares.model.analysis

import ch.scorpion.antares.model.ControlledCircuitRunner
import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.antares.model.inout.DigitalCircuitInOut
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.antares.model.truthtable.TruthTable
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.PortType

class CircuitAnalysisError(msg: String): Error(msg)

class CircuitAnalysisService {

	companion object {
		private val LOG by logger(CircuitAnalysisService::class)
	}

	private val circuitRunner = ControlledCircuitRunner()

	private data class Context(
		val row: Int,
		val circuit: DigitalGraph,
		val truthTable: TruthTable)

	fun analyse(circuit: DigitalGraph): TruthTable {
		LOG.userTrail("Analyzing circuit ${circuit.name.value}")
		val truthTable = buildEmptyTruthTable(circuit)

		(0 until truthTable.rowsCount).forEach { row ->
			circuitRunner.run(circuit, ::setInputs, ::readOutputs, Context(row, circuit, truthTable))
		}

		return  truthTable
	}

	private fun buildEmptyTruthTable(circuit: DigitalGraph): TruthTable {
		val inputs = circuit.graphPorts.filter { it.portType == PortType.INPUT }.map { it as DigitalCircuitInOut }
		if (inputs.isEmpty()) {
			throw CircuitAnalysisError(Translations.getString("antares.circuitAnalysis.noInputs.msg"))
		}
		if (inputs.any { it.bitWidth != BitWidth.BW_1 }) {
			throw CircuitAnalysisError(Translations.getString("antares.circuitAnalysis.multiBit.msg"))
		}

		val outputs = circuit.graphPorts.filter { it.portType == PortType.OUTPUT }.map { it as DigitalCircuitInOut }
		if (outputs.isEmpty()) {
			throw CircuitAnalysisError(Translations.getString("antares.circuitAnalysis.noOutputs.msg"))
		}
		if (outputs.any { it.bitWidth != BitWidth.BW_1 }) {
			throw CircuitAnalysisError(Translations.getString("antares.circuitAnalysis.multiBit.msg"))
		}

		return TruthTable("Analysis Result", inputs.map { it.name!! }, outputs.map { it.name!! })
	}

	private fun setInputs(signalHandler: SignalHandler, c: Any?) {
		val context = c as Context
		(0 until context.truthTable.inputColumnCount).forEach { column ->
			val inputName = context.truthTable.getColumnName(column)
			val input = context.circuit.getGraphInput<DigitalSignal>(inputName)
			input!!.setIncomingSignal(DigitalSignalFactory.of(context.truthTable.getValue(context.row, column)), signalHandler)
		}
	}

	private fun readOutputs(c: Any?) {
		val context = c as Context
		(context.truthTable.inputColumnCount until context.truthTable.columnCount).forEach { column ->
			val outputName = context.truthTable.getColumnName(column)
			val output = context.circuit.getGraphOutput<DigitalSignal>(outputName)
			val bit = output!!.signal!!.bitAt(0)
			if (bit.isDefined) {
				context.truthTable.setValue(context.row, column, bit)
			} else {
				throw CircuitAnalysisError(Translations.getString("antares.circuitAnalysis.undefinedBit.msg", outputName))
			}
		}
	}
}