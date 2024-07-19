package ch.scorpion.antares.hdl.vhdl

import ch.scorpion.antares.hdl.HDLExportTestBenchParams
import ch.scorpion.antares.hdl.HDLModel
import ch.scorpion.antares.hdl.HDLPort
import ch.scorpion.antares.hdl.HdlTestVectorConsumer
import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.testcase.Testcase
import ch.scorpion.antares.model.testcase.Value
import ch.scorpion.antares.model.testcase.parser.TestScript
import ch.scorpion.antares.model.testcase.parser.TestcaseAnalyser
import ch.scorpion.antares.model.testcase.parser.TestcaseInterpreter
import ch.scorpion.antares.model.testcase.parser.TestcaseParser
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.dsl.SemanticError
import ch.scorpion.jabbah.base.dsl.SyntaxError
import ch.scorpion.jabbah.base.io.CodePrinter
import ch.scorpion.jabbah.base.io.Separator
import ch.scorpion.jabbah.base.parser.TextLocation

/**
 * Creates a VHDL test bench for the [Testcase] in [params] and prints it to [out].
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

	companion object {
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
	}

	private val mainComponentName = model.main.elementName

	private val testScript = TestcaseParser(
		params.testCase.testVectors.scriptOrEmpty,
		TestcaseAnalyser(model.main.circuit)
	).parse() as TestScript

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
		for (p in model.main.outputs) {
			out.print("signal ").print(p.name).print(": ").print(getType(p.bitWidth)).println(";")
		}
		out.println()
	}

	private fun printPortMap() {
		out.println("main_0 : $mainComponentName port map (").inc()
		val sep = Separator(out, ",\n")
		for (p in model.main.inputs) {
			sep.check()
			out.print(p.name + " => " + p.name)
		}
		for (p in model.main.outputs) {
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
		testScript.portNames.names.map { it.value!! }.forEach { portName ->
			val saveName = params.renaming.checkName(portName)
			val port = model.main.getPort(saveName) ?:
				throw SemanticError(TextLocation.UNDEFINED, Translations.getString("antares.vhdl.portNameNotFound.error.text", saveName))
			portOrder.add(port)
			out.print(saveName).print(": ").print(getType(port.bitWidth)).println(";")
		}
		out.dec().println("end record;")
		out.println("type test_data_array is array (natural range <>) of test_data_type;")
		return portOrder
	}

	private fun printTestData(portOrder: List<HDLPort>) {
		out.println("constant test_data : test_data_array := (").inc()
		val consumer = VHDLTestVectorConsumer(out, portOrder)
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

		for (p in model.main.inputs) {
			out.print(p.name).print(" <= test_data(").print(loopVar).print(").").print(p.name).println(";")
		}
		out.println("wait for ${params.waitTime} ns;")
		for (p in model.main.outputs) {
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

	private class VHDLTestVectorConsumer(out: CodePrinter, portOrder: List<HDLPort>) : HdlTestVectorConsumer(out, portOrder) {

		private val lineSep = Separator(out, ",\n")

		override fun checkNewLine() {
			lineSep.check()
		}

		override fun printValues(values: List<Value>, isClock: Boolean, clock: DigitalSignal?) {
			// Named association required by VHDL specification if list has only 1 element
			out.print("${line++} => (")
			val sep = Separator(out, ", ")
			values.forEachIndexed { index, value ->
				sep.check()
				val bitWidth = portOrder[index].bitWidth
				when (value.type) {
					Value.Type.NORMAL -> {
						if (isClock && portOrder[index].direction == HDLPort.Direction.IN) {
							out.print(getSimpleValue(bitWidth, '-'))
						} else {
							out.print(value(value.value.toLong() ?: 0UL, bitWidth))
						}
					}
					Value.Type.DONT_CARE -> out.print(getSimpleValue(bitWidth, '-'))
					Value.Type.UNDEFINED -> out.print(getSimpleValue(bitWidth, 'Z'))
					Value.Type.CLOCKED -> if (clock != null) {
						out.print(value(clock))
					} else {
						out.print(value(value.value.toLong() ?: 0UL, bitWidth))
					}
				}
			}
			out.print(")")
		}
	}
}