package ch.scorpion.antares.model.testcase

import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.antares.model.inout.DigitalCircuitInOut
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.antares.model.testcase.TestRunResult.Type.Script
import ch.scorpion.antares.model.testcase.parser.TestScript
import ch.scorpion.antares.model.testcase.parser.TestcaseAnalyser
import ch.scorpion.antares.model.testcase.parser.TestcaseInterpreter
import ch.scorpion.antares.model.testcase.parser.TestcaseParser
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.dsl.*
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.parser.TextLocation
import ch.scorpion.jabbah.graph.model.graph.GraphActivationRecord

/**
 * Runs a circuit test script on the execution script of a [DigitalGraph].
 */
class TestcaseScriptRunner(
	testName: String,
	testScript: TestScript,
	circuit: DigitalGraph,
	private val execScriptAST: Node
) : AbstractTestcaseRunner(testName, testScript, circuit) {

	constructor(testName: String, text: String, circuit: DigitalGraph, execScriptAST: Node): this(
		testName,
		TestcaseParser(text, TestcaseAnalyser(circuit)).parse() as TestScript,
		circuit,
		execScriptAST)

	companion object {
		private val LOG by logger(TestcaseCircuitRunner::class)
	}

	private val memory = Memory(GraphActivationRecord(circuit))

	override fun run(): TestRunResult {
		try {
			val collector = TestVectorCollector()
			portNames = testScript.portNames.names.map { it.value!! }
			TestcaseInterpreter(testScript, circuit, collector).interpret()

			val execScriptInterpreter = BaseModule.interpreterFactory(execScriptAST, memory)

			for (testVector in collector) {
				defineMemory()
				currentTestVector = testVector

				setInputs(null)
				execScriptInterpreter.interpret(keepMemory = true)
				readOutputs(null)
			}

			return TestRunResult(circuit, Script, testName, portNames, determineIsOutput(portNames), collector)
		} catch (e: SyntaxError) {
			return TestRunResult.error(circuit, Script, testName, e.message ?: "Error")
		} catch (e: SemanticError) {
			return TestRunResult.error(circuit, Script, testName, e.message ?: "Error")
		} catch (e: Throwable) {
			LOG.error("Error while running test '${testName}' for circuit '${circuit.name.value}'", e)
			return TestRunResult.error(circuit, Script, testName, Translations.getString("antares.testcase.action.technical.error.txt"))
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
		when (val value = memory.getValue(port.name!!)) {
			is DigitalSignal -> value
			is Long -> DigitalSignalFactory.of(port.bitWidth, value)
			else -> throw RuntimeError(TextLocation.UNDEFINED, Translations.getString("base.dsl.expectedNumber.msg"))
		}

	override fun processInputChanged() { }

	override fun dispose() { }
}