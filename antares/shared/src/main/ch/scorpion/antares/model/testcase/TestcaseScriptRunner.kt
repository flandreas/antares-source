package ch.scorpion.antares.model.testcase

import ch.scorpion.antares.dsl.AntaresInterpreter
import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.inout.DigitalCircuitInOut
import ch.scorpion.antares.model.port.DigitalPort
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
import ch.scorpion.jabbah.graph.container.ContainerDrawing
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.model.StoringGraphActorData
import ch.scorpion.jabbah.graph.model.graph.GraphActivationRecord

/**
 * Runs a circuit test script on the execution script of a [DigitalGraph].
 *
 * @param testName the name of the test to be displayed in the UI.
 * @param testScript the test script to run.
 * @param circuit the [DigitalGraph] to be tested. Used for determining the [PortType] of the port names in [testScript].
 * @property execScriptAST the abstract syntax tree root node of the [circuit]'s execution script.
 * @property inputLogicProvider provides the [Logic] of the input [DigitalPort] with a given name. Used to support
 * implementing "raised signal" literals, because the [Logic] of [DigitalPort]s is defined in the [ContainerDrawing]
 * and not in [DigitalGraph].
 */
class TestcaseScriptRunner(
	testName: String,
	testScript: TestScript,
	circuit: DigitalGraph,
	private val execScriptAST: Node,
	private val inputLogicProvider: (String) -> Logic = { Logic.POSITIVE }
) : AbstractTestcaseRunner(testName, testScript, circuit) {

	constructor(
		testName: String,
		text: String,
		circuit: DigitalGraph,
		execScriptAST: Node,
		inputLogicProvider: (String) -> Logic = { Logic.POSITIVE },
	): this(
		testName,
		TestcaseParser(text, TestcaseAnalyser(circuit)).parse() as TestScript,
		circuit,
		execScriptAST,
		inputLogicProvider)

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
			return TestRunResult.error(circuit, Script, testName, e.toString())
		} catch (e: SemanticError) {
			return TestRunResult.error(circuit, Script, testName, e.toString())
		} catch (e: RuntimeError) {
			return TestRunResult.error(circuit, Script, testName, e.toString())
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
		setInputs(interpreter)
		readOutputs(null)
	}

	private fun defineMemory() {
		memory.reset()
		for (portName in portNames) {
			memory.preset(portName, 0L)
		}
	}

	override fun setInput(input: DigitalCircuitInOut, signal: DigitalSignal) {
		inputPort.name = input.name
		inputPort.logic = inputLogicProvider(input.name!!)
		memory.preset(input.name!!, signal)
		graphActorData = StoringGraphActorData(inputPort, signal)
	}

	override fun readOutput(output: DigitalCircuitInOut): DigitalSignal =
		when (val value = memory.getValue(output.name!!)) {
			is DigitalSignal -> value
			is Long -> DigitalSignalFactory.of(output.bitWidth, value)
			else -> throw RuntimeError(TextLocation.UNDEFINED, Translations.getString("base.dsl.expectedNumber.msg"))
		}

	override fun processInputChanged(context: Any?) {
		(context as AntaresInterpreter).interpret(graphActorData, keepMemory = true)
	}

	override fun dispose() { }
}