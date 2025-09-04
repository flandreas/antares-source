package ch.scorpion.antares.model.testcase

import ch.scorpion.antares.model.ControlledCircuitRunner
import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.antares.model.inout.DigitalCircuitInOut
import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.testcase.TestRunResult.Type.Circuit
import ch.scorpion.antares.model.testcase.TestVector.Type.*
import ch.scorpion.antares.model.testcase.parser.TestScript
import ch.scorpion.antares.model.testcase.parser.TestcaseAnalyser
import ch.scorpion.antares.model.testcase.parser.TestcaseInterpreter
import ch.scorpion.antares.model.testcase.parser.TestcaseParser
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.dsl.SemanticError
import ch.scorpion.jabbah.base.dsl.SyntaxError
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.graph.model.PortType
import kotlin.math.max

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

	companion object {
		private val LOG by logger(TestcaseCircuitRunner::class)
	}

	private val circuitRunner = ControlledCircuitRunner()

	/**
	 * Runs the [TestVector]s contained in [testScript] and returns the [TestRunResult],
	 * whose [TestVectorCollector] output columns contain [MatchedValue] with the actual
	 * result values.
	 */
	override fun run(): TestRunResult {
		try {
			val collector = TestVectorCollector()
			portNames = testScript.portNames.names

			TestcaseInterpreter(testScript, circuit, collector).interpret()

			var maxDuration = 0L
			for (testVector in collector) {
				currentTestVector = testVector

				val duration: Long = when (testVector.type) {
					Top -> circuitRunner.run(circuit, ::setInputs, ::readOutputs)
					RunFirst -> circuitRunner.runStart(circuit, ::setInputs, ::readOutputs)
					RunLine -> circuitRunner.runContinue(circuit, ::setInputs, ::readOutputs)
					RunLast -> circuitRunner.runStop(circuit, ::setInputs, ::readOutputs)
				}
				maxDuration = max(duration, maxDuration)
			}

			return TestRunResult(circuit, Circuit, testName, portNames.map { it.name.value!! }, determineIsOutput(), collector, null, maxDuration)
		} catch (e: SyntaxError) {
			return TestRunResult.error(circuit, TestRunResult.Type.Script, testName, e.message ?: "Error")
		} catch (e: SemanticError) {
			return TestRunResult.error(circuit, TestRunResult.Type.Script, testName, e.message ?: "Error")
		} catch (e: Throwable) {
			LOG.error("Error while running test '${testName}' for circuit '${circuit.name.value}'", e)
			return TestRunResult.error(circuit, Circuit, testName, Translations.getString("antares.testcase.action.technical.error.txt"))
		}
	}

	override fun processInputChanged(context: Any?): Long {
		val t = circuitRunner.scheduler.executionTime
		circuitRunner.proceedUntilQueueEmpty()
		return circuitRunner.scheduler.executionTime - t
	}

	override fun dispose() {
		circuitRunner.dispose()
	}

	override fun setInput(input: DigitalCircuitInOut, signal: DigitalSignal) {
		input.setIncomingSignal(signal, circuitRunner.scheduler)
	}

	override fun readOutput(output: DigitalCircuitInOut): DigitalSignal? = output.signal
}