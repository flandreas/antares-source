package io.antarescircuit.antares.model.testcase

import io.antarescircuit.antares.dsl.AntaresInterpreter
import io.antarescircuit.antares.model.ControlledCircuitRunner
import io.antarescircuit.antares.model.DigitalGraph
import io.antarescircuit.antares.model.Logic
import io.antarescircuit.antares.model.inout.DigitalCircuitInOut
import io.antarescircuit.antares.model.net.Probe
import io.antarescircuit.antares.model.port.DigitalPort
import io.antarescircuit.antares.model.port.DigitalPortImpl
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.model.signal.DigitalSignalFactory
import io.antarescircuit.antares.model.testcase.TestRunResult.Type.Circuit
import io.antarescircuit.antares.model.testcase.TestRunResult.Type.Script
import io.antarescircuit.antares.model.testcase.TestVector.Type.*
import io.antarescircuit.antares.model.testcase.parser.TestScript
import io.antarescircuit.antares.model.testcase.parser.TestcaseAnalyser
import io.antarescircuit.antares.model.testcase.parser.TestcaseInterpreter
import io.antarescircuit.antares.model.testcase.parser.TestcaseParser
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.dsl.*
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.parser.TextLocation
import io.antarescircuit.jabbah.graph.container.ContainerDrawing
import io.antarescircuit.jabbah.graph.model.GraphActorData
import io.antarescircuit.jabbah.graph.model.PortType
import io.antarescircuit.jabbah.graph.model.StoringGraphActorData
import io.antarescircuit.jabbah.graph.model.graph.GraphActivationRecord
import io.antarescircuit.jabbah.graph.model.vertice.SubGraphFunctionContext

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
			portNames = testScript.portNames.names
			TestcaseInterpreter(testScript, circuit, collector).interpret()

			val execScriptInterpreter = BaseModule.interpreterFactory(execScriptAST, memory) as AntaresInterpreter

			for (testVector in collector) {
				currentTestVector = testVector

				val doStart = testVector.type == Top || testVector.type == RunFirst
				runImpl(execScriptInterpreter, doStart)
			}

			return TestRunResult(circuit, Script, testName, portNames.map { it.name.value!! }, determineIsOutput(), collector)
		} catch (e: SyntaxError) {
			return TestRunResult.error(circuit, Script, testName, e.toString())
		} catch (e: SemanticError) {
			return TestRunResult.error(circuit, Script, testName, e.toString())
		} catch (e: RuntimeError) {
			return TestRunResult.error(circuit, Script, testName, e.toString())
		} catch (_: ControlledCircuitRunner.TooManyIterations) {
			return TestRunResult.error(circuit, Circuit, testName, Translations.getString("antares.testcase.results.tooManyIterations.txt"))
		}	catch (e: Throwable) {
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
			memory.preset(portName.name.value!!, 0L)
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

	override fun readOutput(output: Probe): DigitalSignal? =
		when (val value = memory.getValue(output.name!!)) {
			is DigitalSignal -> value
			is Long -> DigitalSignalFactory.of(output.bitWidth, value)
			else -> throw RuntimeError(TextLocation.UNDEFINED, Translations.getString("base.dsl.expectedNumber.msg"))
		}

	override fun processInputChanged(context: Any?): Long {
		// In this environment, SignalHandler is not used and therefore not provided in context.
		// This means that test scripts using external functions based on SignalHandler won't work
		(context as AntaresInterpreter).interpret(
			SubGraphFunctionContext(graphActorData, null, null),
			keepMemory = true
		)
		return 0
	}

	override fun dispose() { }
}