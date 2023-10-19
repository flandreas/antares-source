package ch.scorpion.antares.model.testcase

import ch.scorpion.antares.model.ControlledCircuitRunner
import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.antares.model.inout.DigitalCircuitInOut
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.testcase.TestRunResult.Type.Circuit
import ch.scorpion.antares.model.testcase.parser.TestScript
import ch.scorpion.antares.model.testcase.parser.TestcaseAnalyser
import ch.scorpion.antares.model.testcase.parser.TestcaseInterpreter
import ch.scorpion.antares.model.testcase.parser.TestcaseParser

/**
 * Runs a circuit test script (provided as plain text) on a particular [DigitalGraph].
 */
class TestcaseCircuitRunner(
	testName: String,
	testScript: TestScript,
	circuit: DigitalGraph
) : AbstractTestcaseRunner(testName, testScript, circuit) {

	constructor(testName: String, text: String, circuit: DigitalGraph): this(
		testName,
		TestcaseParser(text, TestcaseAnalyser(circuit)).parse() as TestScript,
		circuit)

	private val circuitRunner = ControlledCircuitRunner()

	/**
	 * Runs the [TestVector]s contained in [text] and returns the [TestRunResult],
	 * whose [TestVectorCollector] output columns contain [MatchedValue] with the actual
	 * result values.
	 */
	override fun run(): TestRunResult {
		try {
			val collector = TestVectorCollector()
			portNames = testScript.portNames.names.map { it.value!! }

			TestcaseInterpreter(testScript, circuit, collector).interpret()

			for (testVector in collector) {
				currentTestVector = testVector
				circuitRunner.run(circuit, ::setInputs, ::readOutputs)
			}

			return TestRunResult(circuit, Circuit, testName, portNames, determineIsOutput(portNames), collector)
		} catch (e: Throwable) {
			return TestRunResult.error(circuit, Circuit, testName, e.message ?: "Error")
		}
	}

	override fun processInputChanged() {
		circuitRunner.proceedUntilQueueEmpty()
	}

	override fun dispose() {
		circuitRunner.dispose()
	}

	override fun setInput(port: DigitalCircuitInOut, signal: DigitalSignal) {
		port.setIncomingSignal(signal, circuitRunner.scheduler)
	}

	override fun readOutput(port: DigitalCircuitInOut): DigitalSignal? {
		return port.signal
	}
}