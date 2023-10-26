package ch.scorpion.antares.model.testcase

import ch.scorpion.antares.dsl.AntaresInterpreter
import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.antares.model.inout.DigitalCircuitInOut
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.antares.model.testcase.TestRunResult.Type.Script
import ch.scorpion.antares.model.testcase.TestVector.Type.*
import ch.scorpion.antares.model.testcase.parser.TestScript
import ch.scorpion.antares.model.testcase.parser.TestcaseAnalyser
import ch.scorpion.antares.model.testcase.parser.TestcaseInterpreter
import ch.scorpion.antares.model.testcase.parser.TestcaseParser
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.dsl.*
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.parser.TextLocation
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.model.StoringGraphActorData
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

	/** Dummy input port used to support feature "raised input" of [AntaresInterpreter].*/
	private val inputPort = DigitalPortImpl(PortType.INPUT)

	/** Dummy [GraphActorData] that returns [inputPort] after it has received a new value.*/
	private var graphActorData: GraphActorData = StoringGraphActorData(inputPort, null)

	override fun run(): TestRunResult {
		try {
			val collector = TestVectorCollector()
			portNames = testScript.portNames.names.map { it.value!! }
			TestcaseInterpreter(testScript, circuit, collector).interpret()

			val execScriptInterpreter = BaseModule.interpreterFactory(execScriptAST, memory) as AntaresInterpreter

			for (testVector in collector) {
				currentTestVector = testVector

				val doStart = testVector.type == Top || testVector.type == RunFirst
				runImpl(execScriptInterpreter, doStart)
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

	private fun runImpl(interpreter: AntaresInterpreter, doStart: Boolean) {
		if (doStart) {
			defineMemory()
			interpreter.executionStarted()
		}
		setInputs(null)
		interpreter.interpret(graphActorData, keepMemory = true)
		readOutputs(null)
	}

	private fun defineMemory() {
		memory.clear()
		for (portName in portNames) {
			memory.preset(portName, 0L)
		}
	}

	override fun setInput(port: DigitalCircuitInOut, signal: DigitalSignal) {
		memory.preset(port.name!!, signal)
		inputPort.name = port.name
		graphActorData = StoringGraphActorData(inputPort, signal)
	}

	override fun readOutput(port: DigitalCircuitInOut): DigitalSignal =
		when (val value = memory.getValue(port.name!!)) {
			is DigitalSignal -> value
			is Long -> DigitalSignalFactory.of(port.bitWidth, value)
			else -> throw RuntimeError(TextLocation.UNDEFINED, Translations.getString("base.dsl.expectedNumber.msg"))
		}

	override fun processInputChanged() { }

	override fun dispose() { }
}