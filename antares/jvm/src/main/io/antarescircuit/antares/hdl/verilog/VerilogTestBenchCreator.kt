package io.antarescircuit.antares.hdl.verilog

import io.antarescircuit.antares.hdl.HDLExportTestBenchParams
import io.antarescircuit.antares.hdl.HDLModel
import io.antarescircuit.antares.hdl.HDLPort
import io.antarescircuit.antares.hdl.TestBenchColumn
import io.antarescircuit.antares.hdl.TestBenchRole
import io.antarescircuit.antares.hdl.resolveTestBenchColumns
import io.antarescircuit.antares.model.port.DigitalPort
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.model.testcase.TestVector
import io.antarescircuit.antares.model.testcase.TestVectorConsumer
import io.antarescircuit.antares.model.testcase.Testcase
import io.antarescircuit.antares.model.testcase.Value
import io.antarescircuit.antares.model.testcase.parser.TestScript
import io.antarescircuit.antares.model.testcase.parser.TestcaseAnalyser
import io.antarescircuit.antares.model.testcase.parser.TestcaseInterpreter
import io.antarescircuit.antares.model.testcase.parser.TestcaseParser
import io.antarescircuit.jabbah.base.dsl.SemanticError
import io.antarescircuit.jabbah.base.dsl.SyntaxError
import io.antarescircuit.jabbah.base.io.CodePrinter

/**
 * Creates a Verilog test bench for the [Testcase] in [params] and prints it to [out].
 * Each test vector applies the inputs, waits [HDLExportTestBenchParams.waitTime] ns and
 * compares the outputs using `!==`, so that expected high impedance ("Z") values are checked as well.
 * In/out pins are driven by a register that is released ("Z") until the test script drives
 * the pin (column marked as input, e.g. ">A"); they are checked if marked as output (e.g. "<A").
 *
 * @throws SyntaxError if the [Testcase] script cannot be successfully parsed
 * @throws SemanticError if the [Testcase] script references non-existing [DigitalPort]s
 */
class VerilogTestBenchCreator(
	private val out: CodePrinter,
	private val model: HDLModel,
	private val baseName: String,
	private val params: HDLExportTestBenchParams,
) {

	private val main = model.main

	private val testScript = TestcaseParser(
		params.testCase.testVectors.scriptOrEmpty,
		TestcaseAnalyser(main.circuit)
	).parse() as TestScript

	/** Name of the variable counting failed assertions. */
	private val errorsVar = uniqueName("errors")

	/** Names of the registers driving the in/out pins. */
	private val drivers: Map<HDLPort, String> = main.inOuts.associateWith { uniqueName("${it.name}_drv") }

	fun print() {
		out.print("// Test bench for ").println(baseName)
		out.print("module ").print(params.testBenchName).println(";").inc()
		printDeclarations()
		printInstance()
		printTests()
		out.dec().println("endmodule")
	}

	private fun printDeclarations() {
		for (p in main.inputs) {
			out.print("reg ").print(Verilog.range(p.bitWidth)).print(p.name).println(";")
		}
		for (p in main.outputs + main.inOuts) {
			out.print("wire ").print(Verilog.range(p.bitWidth)).print(p.name).println(";")
		}
		for ((p, driver) in drivers) {
			out.print("reg ").print(Verilog.range(p.bitWidth)).print(driver).println(";")
		}
		out.print("integer ").print(errorsVar).println(";")
		for ((p, driver) in drivers) {
			out.print("assign ").print(p.name).print(" = ").print(driver).println(";")
		}
		out.println()
	}

	private fun printInstance() {
		out.print(main.entityName).print(" ").print(uniqueName("main_0")).println(" (").inc()
		out.println((main.inputs + main.outputs + main.inOuts).joinToString(",\n") { ".${it.name}(${it.name})" })
		out.dec().println(");")
		out.println()
	}

	private fun printTests() {
		out.println("initial begin").inc()
		out.print(errorsVar).println(" = 0;")
		for ((p, driver) in drivers) {
			out.print(driver).print(" = ").print(Verilog.fill(p.bitWidth, 'z')).println(";")
		}
		val consumer = VerilogTestVectorConsumer(resolveTestBenchColumns(testScript, main, params.renaming))
		TestcaseInterpreter(testScript, main.circuit, consumer).interpret()
		out.print("if (").print(errorsVar).println(" == 0)")
		out.inc().println("\$display(\"All tests passed.\");").dec()
		out.println("else")
		out.inc().print("\$display(\"%0d assertion(s) failed.\", ").print(errorsVar).println(");").dec()
		out.println("\$finish;")
		out.dec().println("end")
	}

	private fun uniqueName(base: String): String {
		var name = base
		var index = 0
		while (main.getPort(name, ignoreCase = false) != null) {
			name = "$base${index++}"
		}
		return name
	}

	private inner class VerilogTestVectorConsumer(private val columns: List<TestBenchColumn>) : TestVectorConsumer {

		/** Index of the test step, reported in failed assertions. */
		private var line = 0

		override fun consume(testVector: TestVector) {
			// A clocked value is preceded by the inverted and the actual clock value without checking outputs
			val clockedValue = testVector.values.firstOrNull { it.type == Value.Type.CLOCKED }
			if (clockedValue != null) {
				printStep(testVector.values, true, clockedValue.value.not())
				printStep(testVector.values, true, clockedValue.value)
			}
			printStep(testVector.values, false, null)
		}

		private fun printStep(values: List<Value>, isClockPhase: Boolean, clock: DigitalSignal?) {
			val checks = mutableListOf<Pair<HDLPort, String>>()
			values.forEachIndexed { index, value ->
				val port = columns[index].port
				when (columns[index].role) {
					TestBenchRole.DRIVE -> out.print(drivers[port] ?: port.name).print(" = ").print(inputValue(port, value, clock)).println(";")
					TestBenchRole.CHECK -> expectedValue(port, value, isClockPhase)?.let { checks.add(port to it) }
					TestBenchRole.IGNORE -> {}
				}
			}
			out.print("#").print(params.waitTime).println(";")
			for ((port, expected) in checks) {
				out.print("if (").print(port.name).print(" !== ").print(expected).println(") begin").inc()
				out.print("\$display(\"assertion failed for ").print(port.name).print(" on line ").print(line)
					.print(", expected %b, actual is %b\", ").print(expected).print(", ").print(port.name).println(");")
				out.print(errorsVar).print(" = ").print(errorsVar).println(" + 1;")
				out.dec().println("end")
			}
			line++
		}

		private fun inputValue(port: HDLPort, value: Value, clock: DigitalSignal?): String =
			when (value.type) {
				Value.Type.NORMAL -> normalValue(port, value)
				Value.Type.DONT_CARE -> Verilog.fill(port.bitWidth, 'x')
				Value.Type.UNDEFINED -> Verilog.fill(port.bitWidth, 'z')
				Value.Type.CLOCKED -> clock?.let { Verilog.value(it) } ?: normalValue(port, value)
			}

		/** Returns the expected literal, or `null` if the output is not to be checked. */
		private fun expectedValue(port: HDLPort, value: Value, isClockPhase: Boolean): String? =
			when (value.type) {
				Value.Type.NORMAL, Value.Type.CLOCKED -> if (isClockPhase) null else normalValue(port, value)
				Value.Type.DONT_CARE -> null
				Value.Type.UNDEFINED -> Verilog.fill(port.bitWidth, 'z')
			}

		/** Keeps partially high-impedance values such as 0b1Z01 instead of turning them into 0. */
		private fun normalValue(port: HDLPort, value: Value): String =
			Verilog.value(value.value.ofWidth(port.bitWidth))
	}
}
