package io.antarescircuit.antares.hdl.vhdl

import io.antarescircuit.antares.hdl.HDLExportTestBenchParams
import io.antarescircuit.antares.hdl.HDLModel
import io.antarescircuit.antares.hdl.HDLPort
import io.antarescircuit.antares.hdl.TestBenchRole
import io.antarescircuit.antares.hdl.resolveTestBenchColumns
import io.antarescircuit.antares.model.port.DigitalPort
import io.antarescircuit.antares.model.signal.BitWidth
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
import io.antarescircuit.jabbah.base.io.Separator

/**
 * Creates a VHDL test bench for the [Testcase] in [params] and prints it to [out].
 * In/out pins are driven by a signal that is released ("Z") until the test script drives
 * the pin (column marked as input, e.g. ">A"); they are checked if marked as output (e.g. "<A").
 *
 * @throws SyntaxError if the [Testcase] script cannot be successfully parsed
 * @throws SemanticError if the [Testcase] script references non-existing [DigitalPort]s
 */
class VHDLTestBenchCreator(
	out: CodePrinter,
	private val model: HDLModel,
	private val baseName: String,
	private val params: HDLExportTestBenchParams
) : AbstractVHDLCreator(out) {

	private val mainComponentName = model.main.elementName

	private val testScript = TestcaseParser(
		params.testCase.testVectors.scriptOrEmpty,
		TestcaseAnalyser(model.main.circuit)
	).parse() as TestScript

	private val columns = resolveTestBenchColumns(testScript, model.main, params.renaming)

	private val drivenPorts = columns.filter { it.role == TestBenchRole.DRIVE }.map { it.port }.toSet()

	private val checkedPorts = columns.filter { it.role == TestBenchRole.CHECK }.map { it.port }.toSet()

	/** Names of the signals driving the in/out pins. */
	private val drivers: Map<HDLPort, String> = model.main.inOuts.associateWith { uniqueName("${it.name}_drv") }

	fun print() {
		out.print("-- Test bench for ").println(baseName)
		printImports()
		printEntity()
		printBehaviour()
	}

	private fun printEntity() {
		out.print("entity ").print(params.testBenchName).println(" is")
		out.print("end ").print(params.testBenchName).println(";")
		out.println()
	}

	private fun printBehaviour() {
		out.print("architecture Behavioral of ").print(params.testBenchName).println(" is").inc()
		printComponent()
		printSignals()
		printToString()
		out.dec().println("begin").inc()
		printPortMap()
		printProcess()
		out.dec().println("end Behavioral;")
	}

	private fun printComponent() {
		out.println("component $mainComponentName").inc()
		printEntityPorts(model.main)
		out.dec().println("end component;")
		out.println()
	}

	private fun printSignals() {
		for (p in model.main.inputs) {
			out.print("signal ").print(p.name).print(": ").print(getType(p.bitWidth)).println(";")
		}
		for (p in model.main.outputs + model.main.inOuts) {
			out.print("signal ").print(p.name).print(": ").print(getType(p.bitWidth)).println(";")
		}
		for ((p, driver) in drivers) {
			out.print("signal ").print(driver).print(": ").print(getType(p.bitWidth))
				.print(" := ").print(getSimpleValue(p.bitWidth, 'Z')).println(";")
		}
		out.println()
	}

	private fun printPortMap() {
		for ((p, driver) in drivers) {
			out.print(p.name).print(" <= ").print(driver).println(";")
		}
		out.println("main_0 : $mainComponentName port map (").inc()
		val sep = Separator(out, ",\n")
		for (p in model.main.inputs) {
			sep.check()
			out.print(p.name + " => " + p.name)
		}
		for (p in model.main.outputs + model.main.inOuts) {
			sep.check()
			out.print(p.name + " => " + p.name)
		}
		out.println(");").dec()
	}

	private fun printProcess() {
		out.println("process").inc()
		val portOrder = printTypePattern()
		printTestData(portOrder)
		out.dec().println("begin").inc()
		printLoop()
		out.dec().println("end process;")
	}

	private fun printTypePattern(): List<HDLPort> {
		val portOrder = mutableListOf<HDLPort>()
		out.println("type test_data_type is record").inc()
		for (column in columns) {
			portOrder.add(column.port)
			out.print(column.port.name).print(": ").print(getType(column.port.bitWidth)).println(";")
		}
		out.dec().println("end record;")
		out.println("type test_data_array is array (natural range <>) of test_data_type;")
		return portOrder
	}

	private fun printTestData(portOrder: List<HDLPort>) {
		out.println("constant test_data : test_data_array := (").inc()
		val consumer = VHDLTestVectorConsumer(portOrder, columns.map { it.role })
		val interpreter = TestcaseInterpreter(testScript, model.main.circuit, consumer)
		interpreter.interpret()
		out.println(");").dec()
	}

	private fun printLoop() {
		var loopVar = "i"
		var lv = 0
		while (model.main.getPort(loopVar) != null) {
			loopVar = "i${lv++}"
		}
		out.print("for ").print(loopVar).println(" in test_data'range loop").inc()

		for (p in model.main.inputs + model.main.inOuts) {
			if (p in drivenPorts) {
				out.print(drivers[p] ?: p.name).print(" <= test_data(").print(loopVar).print(").").print(p.name).println(";")
			}
		}
		out.println("wait for ${params.waitTime} ns;")
		for (p in (model.main.outputs + model.main.inOuts).filter { it in checkedPorts }) {
			out.print("assert std_match(").print(p.name).print(", test_data(").print(loopVar).print(").").print(p.name).print(")")
			out.print(" OR (")
				.print(p.name).print(" = ").print(getSimpleValue(p.bitWidth, 'Z'))
				.print(" AND test_data(").print(loopVar).print(").").print(p.name).print(" = ").print(getSimpleValue(p.bitWidth, 'Z'))
				.println(")")

			out.inc().print("report \"assertion failed for ").print(p.name).print(" on line \"")
				.print(" & integer'image(").print(loopVar).print(")")
				.print(" & \", expected \" & ").print(convertFunc(p)).print("(test_data(").print(loopVar).print(").").print(p.name).print(")")
				.print(" & \", actual is \" & ").print(convertFunc(p)).print("(").print(p.name).println(")")

			out.println("severity error;").dec()
		}

		out.dec().println("end loop;")
	}

	private fun printToString() {
		out.println("""
			function to_string(v: std_logic_vector) return string is
			  variable s : string (1 to v'length) := (others => NUL);
			  variable si : integer := 1; 
			begin
			  for i in v'range loop
			    s(si) := std_logic'image(v((i)))(2);
			    si := si + 1;
			  end loop;
			  return s;
			end function;			
		""".trimIndent()).println()
	}

	private fun convertFunc(p: HDLPort): String {
		if (p.bitWidth.width > 1) {
			return "to_string"
		}
		return "std_logic'image"
	}

	private fun getSimpleValue(bitWidth: BitWidth, c: Char): String {
		if (bitWidth == BitWidth.BW_1) {
			return "'$c'"
		}
		val sb = StringBuilder("\"")
		(0 until bitWidth.width).forEach { _ ->
			sb.append(c)
		}
		return sb.append("\"").toString()
	}

	/** Returns [base], extended by a number if a port of the circuit already uses that name. */
	private fun uniqueName(base: String): String {
		var name = base
		var index = 0
		while (model.main.getPort(name) != null) {
			name = "$base${index++}"
		}
		return name
	}

	private inner class VHDLTestVectorConsumer(
		private val portOrder: List<HDLPort>,
		private val roles: List<TestBenchRole>,
	) : TestVectorConsumer {

		private var line = 0
		private val lineSep = Separator(out, ",\n")

		override fun consume(testVector: TestVector) {
			val clockedSignal = testVector.values.firstOrNull { it.type == Value.Type.CLOCKED }
			if (clockedSignal != null) {
				lineSep.check()
				printValues(testVector.values, true, clockedSignal.value.not())
				lineSep.check()
				printValues(testVector.values, true, clockedSignal.value)
			}
			lineSep.check()
			printValues(testVector.values, false, null)
		}

		private fun printValues(values: List<Value>, isClock: Boolean, clock: DigitalSignal?) {
			// Named association required by VHDL specification if list has only 1 element
			out.print("${line++} => (")
			val sep = Separator(out, ", ")
			values.forEachIndexed { index, value ->
				sep.check()
				val bitWidth = portOrder[index].bitWidth
				when (value.type) {
					Value.Type.NORMAL -> {
						// Checked values are irrelevant while the clock is toggled
						if (isClock && roles[index] == TestBenchRole.CHECK) {
							out.print(getSimpleValue(bitWidth, '-'))
						} else {
							out.print(value(value.value.ofWidth(bitWidth)))
						}
					}
					Value.Type.DONT_CARE -> out.print(getSimpleValue(bitWidth, '-'))
					Value.Type.UNDEFINED -> out.print(getSimpleValue(bitWidth, 'Z'))
					Value.Type.CLOCKED -> if (clock != null) {
						out.print(value(clock))
					} else {
						out.print(value(value.value.ofWidth(bitWidth)))
					}
				}
			}
			out.print(")")
		}
	}
}