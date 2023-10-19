package ch.scorpion.antares.model.testcase

import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.antares.model.inout.DigitalCircuitInOut
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.testcase.TestRunResult.Type.Script
import ch.scorpion.antares.model.testcase.parser.TestScript
import ch.scorpion.antares.model.testcase.parser.TestcaseAnalyser
import ch.scorpion.antares.model.testcase.parser.TestcaseInterpreter
import ch.scorpion.antares.model.testcase.parser.TestcaseParser
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.dsl.Interpreter
import ch.scorpion.jabbah.base.dsl.Memory
import ch.scorpion.jabbah.base.module.BaseModule

/**
 * Runs a circuit test script on the execution script of a [DigitalGraph].
 */
class TestcaseScriptRunner(
	testName: String,
	testScript: TestScript,
	circuit: DigitalGraph
) : AbstractTestcaseRunner(testName, testScript, circuit) {

	constructor(testName: String, text: String, circuit: DigitalGraph): this(
		testName,
		TestcaseParser(text, TestcaseAnalyser(circuit)).parse() as TestScript,
		circuit)

	private val memory = Memory()

	override fun run(): TestRunResult {
		require(StringUtils.isNotBlank(circuit.script)) { "Circuit's script must not be empty"}

		try {
			val collector = TestVectorCollector()
			portNames = testScript.portNames.names.map { it.value!! }
			TestcaseInterpreter(testScript, circuit, collector).interpret()

			val execScriptInterpreter = createExecScriptInterpreter(circuit.script!!, memory)

			for (testVector in collector) {
				defineMemory()
				currentTestVector = testVector

				setInputs(null)
				execScriptInterpreter.interpret(keepMemory = true)
				readOutputs(null)
			}

			return TestRunResult(circuit, Script, testName, portNames, determineIsOutput(portNames), collector)
		} catch (e: Throwable) {
			return TestRunResult.error(circuit, Script, testName, e.message ?: "Error")
		}
	}

	private fun defineMemory() {
		memory.clear()
		for (portName in portNames) {
			memory.preset(portName, 0)
		}
	}

	override fun setInput(port: DigitalCircuitInOut, signal: DigitalSignal) {
		memory.preset(port.name!!, signal)
	}

	override fun readOutput(port: DigitalCircuitInOut): DigitalSignal? =
		memory.getValue(port.name!!) as DigitalSignal?

	override fun processInputChanged() { }

	override fun dispose() { }

	private fun createExecScriptInterpreter(circuitScript: String, memory: Memory): Interpreter {
		val parser = BaseModule.parserFactory(circuitScript, null)
		return BaseModule.interpreterFactory(parser.parse(), memory)
	}
}