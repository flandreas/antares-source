package ch.scorpion.antares.model.testcase

import ch.scorpion.antares.model.ControlledCircuitRunner
import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.antares.model.inout.DigitalCircuitInOut
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.antares.model.testcase.parser.TestScript
import ch.scorpion.antares.model.testcase.parser.TestcaseAnalyser
import ch.scorpion.antares.model.testcase.parser.TestcaseInterpreter
import ch.scorpion.antares.model.testcase.parser.TestcaseParser
import ch.scorpion.jabbah.execution.SignalHandler

/**
 * The result provided by a [TestcaseRunner].
 *
 * @property testName the name of the [Testcase]
 * @property names the name of the input and output ports from the plaintext test script
 * @property isOutput `true` indicates that the corresponding column refers to an output
 * @property collector contains the collected [TestVector]s whose output column values
 * have been replaced with [MatchedValue] containing the test result.
 * @property errorMessage the error message if parsing or analysing the [Testcase] failed, `null` otherwise
 */
data class TestRunResult(
	val testName: String,
	val names: List<String>,
	val isOutput: List<Boolean>,
	val collector: TestVectorCollector,
	val errorMessage: String? = null
) {
	companion object {
		fun error(testName: String, msg: String): TestRunResult =
			TestRunResult(testName, emptyList(), emptyList(), TestVectorCollector(), msg)
	}

	/** Returns the number of failed [TestVector]s.*/
	val failedCount: Int get() {
		val failedVectors = mutableSetOf<TestVector>()
		for (column in names.indices) {
			if (isOutput[column]) {
				collector.testVectors
					.filter { it.isFailed(column) }
					.forEach { failedVectors.add(it) }
			}
		}
		return failedVectors.size
	}
}

/**
 * Runs a circuit test script (provided as plain text) on a particular [DigitalGraph].
 */
class TestcaseRunner(
	private val testName: String,
	private val text: String,
	private val circuit: DigitalGraph
) {
	private val circuitRunner = ControlledCircuitRunner()
	private lateinit var portNames: List<String>
	private lateinit var currentTestVector: TestVector

	fun dispose() {
		circuitRunner.dispose()
	}

	/**
	 * Runs the [TestVector]s contained in [text] and returns the [TestRunResult],
	 * whose [TestVectorCollector] output columns contain [MatchedValue] with the actual
	 * result values.
	 */
	fun run(): TestRunResult {
		try {
			val collector = TestVectorCollector()
			val testScript = TestcaseParser(text, TestcaseAnalyser(circuit)).parse() as TestScript
			portNames = testScript.portNames.names.map { it.value!! }

			TestcaseInterpreter(testScript, circuit, collector).interpret()

			for (testVector in collector) {
				currentTestVector = testVector
				circuitRunner.run(circuit, ::setInputs, ::readOutputs)
			}

			return TestRunResult(testName, portNames, determineIsOutput(), collector)
		} catch (e: Throwable) {
			return TestRunResult.error(testName, e.message ?: "Error")
		}
	}

	private fun determineIsOutput(): List<Boolean> = portNames.map {
		val port = circuit.getGraphPort<DigitalSignal>(it)
		port is DigitalCircuitInOut && port.portType.isOutput
	}.toList()

	@Suppress("UNUSED_PARAMETER")
	private fun setInputs(signalHandler: SignalHandler, context: Any?) {
		setInputsFiltered(signalHandler) { it != Value.Type.CLOCKED }
		setInputsFiltered(signalHandler) { it == Value.Type.CLOCKED }
	}

	private fun setInputsFiltered(signalHandler: SignalHandler, filter: (Value.Type) -> Boolean) {
		portNames.forEachIndexed { index, portName ->
			val port = circuit.getGraphPort<DigitalSignal>(portName)
			if (port is DigitalCircuitInOut && port.portType.isInput) {
				val value = currentTestVector.getValue(index)
				if (filter(value.type)) {
					val signal = DigitalSignalFactory.of(port.bitWidth, value.value)
					port.setIncomingSignal(signal, signalHandler)
					circuitRunner.proceedUntilQueueEmpty()
				}
			}
		}
	}

	@Suppress("UNUSED_PARAMETER")
	private fun readOutputs(context: Any?) {
		portNames.forEachIndexed { index, portName ->
			val port = circuit.getGraphPort<DigitalSignal>(portName)
			if (port is DigitalCircuitInOut && port.portType.isOutput) {
				val expected = currentTestVector.getValue(index)
				val output = port.signal
				val matchedValue = MatchedValue(expected, output!!)
				currentTestVector.setValue(index, matchedValue)
			}
		}
	}
}