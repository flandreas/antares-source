package ch.scorpion.antares.model.analysis

import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.antares.model.truthtable.TruthTable
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.time.SystemSpeed
import ch.scorpion.jabbah.execution.scheduler.ManualSchedulerTask
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import ch.scorpion.jabbah.execution.scheduler.SchedulerImpl
import ch.scorpion.jabbah.execution.speed.CurrentSystemSpeedCategory
import ch.scorpion.jabbah.graph.model.PortType

class CircuitAnalysisError(msg: String): Error(msg)

class CircuitAnalysisService {

	companion object {
		private const val MAX_ITERATION_COUNT = 100
	}

	fun analyse(circuit: DigitalGraph): TruthTable {
		val scheduler = SchedulerImpl(
			currentSystemSpeedCategory = CurrentSystemSpeedCategory(SystemSpeed(SystemSpeed.MAX_SPEED)),
			task = ManualSchedulerTask()
		)
		val truthTable = buildEmptyTruthTable(circuit)

		(0 until truthTable.rowsCount).forEach { row -> fill(row, circuit, truthTable, scheduler) }

		return  truthTable
	}

	private fun buildEmptyTruthTable(circuit: DigitalGraph): TruthTable {
		val inputs = circuit.graphPorts.filter { it.portType == PortType.INPUT }
		if (inputs.isEmpty()) {
			throw CircuitAnalysisError(Translations.getString("antares.circuitAnalysis.noInputs.msg"))
		}
		val outputs = circuit.graphPorts.filter { it.portType == PortType.OUTPUT }
		if (outputs.isEmpty()) {
			throw CircuitAnalysisError(Translations.getString("antares.circuitAnalysis.noOutputs.msg"))
		}

		return TruthTable("Analysis Result", inputs.map { it.name!! }, outputs.map { it.name!! })
	}

	private fun fill(row: Int, circuit: DigitalGraph, truthTable: TruthTable, scheduler: Scheduler) {
		startSimulation(circuit, scheduler)

		try {
			setInputs(row, circuit, truthTable, scheduler)

			var iterationCount = 0
			while (!scheduler.isQueueEmpty) {
				// Check for too many iterations
				iterationCount++
				if (iterationCount > MAX_ITERATION_COUNT) {
					throw CircuitAnalysisError(Translations.getString("antares.circuitAnalysis.tooManyIterations.msg"))
				}
				scheduler.execute()
			}
			readOutputs(row, circuit, truthTable)
		} catch (e: CircuitAnalysisError) {
			throw e
		} finally {
			stopSimulation(circuit, scheduler)
		}
	}

	private fun startSimulation(circuit: DigitalGraph, scheduler: Scheduler) {
		scheduler.isActive = true
		circuit.formNet(scheduler)
		circuit.executionInitialize(scheduler)
		circuit.executionStart(scheduler)
	}

	private fun stopSimulation(circuit: DigitalGraph, scheduler: Scheduler) {
		scheduler.isActive = false
		circuit.executionStopped(scheduler)
	}

	private fun setInputs(row: Int, circuit: DigitalGraph, truthTable: TruthTable, scheduler: Scheduler) {
		(0 until truthTable.inputColumnCount).forEach { column ->
			val inputName = truthTable.getColumnName(column)
			val input = circuit.getGraphInput<DigitalSignal>(inputName)
			input!!.setIncomingSignal(DigitalSignalFactory.of(truthTable.getValue(row, column)), scheduler)
		}
	}

	private fun readOutputs(row: Int, circuit: DigitalGraph, truthTable: TruthTable) {
		(truthTable.inputColumnCount until truthTable.columnCount).forEach { column ->
			val outputName = truthTable.getColumnName(column)
			val output = circuit.getGraphOutput<DigitalSignal>(outputName)
			val bit = output!!.signal!!.bitAt(0)
			if (bit.isDefined) {
				truthTable.setValue(row, column, bit)
			} else {
				throw CircuitAnalysisError(Translations.getString("antares.circuitAnalysis.undefinedBit.msg", outputName))
			}
		}
	}
}