package ch.scorpion.antares.model.testcase

import ch.scorpion.antares.model.ControlledCircuitRunner
import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.antares.model.inout.DigitalCircuitInOut
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.antares.model.testcase.parser.TestScript
import ch.scorpion.antares.model.testcase.parser.TestcaseInterpreter
import ch.scorpion.antares.model.testcase.parser.TestcaseParser
import ch.scorpion.jabbah.execution.SignalHandler

/**
 * The result provided by a [TestcaseRunner].
 *
 * @property names the name of the input and output ports from the plaintext test script
 * @property isOutput `true` indicates that the corresponding column refers to an output
 * @property collector contains the collected [TestVector]s whose output column values
 * have been replaced with [MatchedValue] containing the test result.
 */
data class TestRunResult(
	val names: List<String>,
	val isOutput: List<Boolean>,
	val collector: TestVectorCollector
)

/**
 * Runs a circuit test script (provided as plain text) on a particular [DigitalGraph].
 */
class TestcaseRunner(
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
		val collector = TestVectorCollector()

		val testScript = TestcaseParser(text).parse() as TestScript
		portNames = testScript.portNames.names.map { it.value!! }

		TestcaseInterpreter(testScript, collector).interpret()

		for (testVector in collector) {
			currentTestVector = testVector
			circuitRunner.run(circuit, ::setInputs, ::readOutputs)
		}

		return TestRunResult(portNames, determineIsOutput(), collector)
	}

	private fun determineIsOutput(): List<Boolean> = portNames.map {
		val port = circuit.getGraphPort<DigitalSignal>(it)
		port is DigitalCircuitInOut && port.portType.isOutput
	}.toList()

	private fun setInputs(signalHandler: SignalHandler) {
		portNames.forEachIndexed { index, portName ->
			val port = circuit.getGraphPort<DigitalSignal>(portName)
			if (port is DigitalCircuitInOut && port.portType.isInput) {
				val value = currentTestVector.getValue(index).value
				val signal = DigitalSignalFactory.of(port.bitWidth, value)
				port.setIncomingSignal(signal, signalHandler)
			}
		}
	}

	private fun readOutputs() {
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