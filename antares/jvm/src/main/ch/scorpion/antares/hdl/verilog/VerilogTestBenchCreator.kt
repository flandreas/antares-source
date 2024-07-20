package ch.scorpion.antares.hdl.verilog

import ch.scorpion.antares.hdl.HDLExportTestBenchParams
import ch.scorpion.antares.hdl.HDLModel
import ch.scorpion.antares.hdl.HDLPort
import ch.scorpion.antares.hdl.HdlTestVectorConsumer
import ch.scorpion.antares.hdl.vhdl.HDLException
import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.signal.BitOperation
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
import ch.scorpion.jabbah.base.io.StringCodePrinter
import ch.scorpion.jabbah.base.parser.TextLocation

/**
 * Creates a Verilog test bench for the [Testcase] in [params] and prints it to [out].
 *
 * @throws SyntaxError if the [Testcase] script cannot be successfully parsed
 * @throws SemanticError if the [Testcase] script references non-existing [DigitalPort]s
 */
class VerilogTestBenchCreator(
    out: CodePrinter,
    private val model: HDLModel,
    private val baseName: String,
    private val params: HDLExportTestBenchParams
) : AbstractVerilogCreator(out) {

    private val mainComponentName = model.main.elementName

    private val testScript = TestcaseParser(
        params.testCase.testVectors.scriptOrEmpty,
        TestcaseAnalyser(model.main.circuit)
    ).parse() as TestScript

    fun print() {
        out.print("// Test bench for ").println(baseName)
        out.println("`timescale 1ns/1ns").println()
        out.print("module ").print(params.testBenchName).println(";").inc()

        printLocalPortDeclaration()
        printModuleInstantiation()
        printBody()

        out.dec().println("endmodule")
    }

    private fun printLocalPortDeclaration() {
        for (port in model.main.ports) {
            out.print(getSignalDeclarationCode(port)).println(";")
        }
    }

    private fun getSignalDeclarationCode(p: HDLPort): String {
        val s = StringBuffer()
        when (p.direction) {
            HDLPort.Direction.IN -> s.append("wire ")
            HDLPort.Direction.OUT -> s.append("reg ")
            HDLPort.Direction.INOUT -> s.append("/* Invalid port */")
        }
        if (p.bitWidth.width > 1) {
            s.append(getType(p.bitWidth)).append(" ")
        }
        s.append(p.name)

        return s.toString()
    }

    private fun printModuleInstantiation() {
        out.println()
        out.print(mainComponentName).print(' ').print(mainComponentName).print("0 (").println()
        out.inc()
        val sep = Separator(out, ",\n")
        for (port in model.main.ports) {
            sep.check()
            out.print('.').print(port.name).print('(').print(port.name).print(')')
        }
        out.dec().println().print(");").println().println()
    }

    private fun printBody() {
        val dataOrder = mutableListOf<HDLPort>()
        val inputsInOrder = mutableListOf<HDLPort>()
        val outputsInOrder = mutableListOf<HDLPort>()

        testScript.portNames.names
            .map { it.value!! }
            .forEach { name ->
                val saveName = params.renaming.checkName(name)
                model.main.ports.firstOrNull { it.name == saveName }?.let { port ->
                    dataOrder.add(port)
                    if (port.direction == HDLPort.Direction.OUT) {
                        inputsInOrder.add(port)
                    } else {
                        outputsInOrder.add(port)
                    }
                } ?: throw SemanticError(TextLocation.UNDEFINED, Translations.getString("antares.vhdl.portNameNotFound.error.text", saveName))
            }

        val rowBits = dataOrder.sumOf { it.bitWidth.width }
        val outTmp = StringCodePrinter()
        val consumer = VerilogTestVectorConsumer(outTmp, dataOrder, rowBits)
        TestcaseInterpreter(testScript, model.main.circuit, consumer).interpret()
        val lineCount = consumer.line

        val patternRange1 = if (rowBits == 1) "" else "[${rowBits - 1}:0]"
        val patternRange2 = if (lineCount == 1) "" else "[0:${lineCount - 1}]"

        out.print("reg ").print(patternRange1).print(" patterns").print(patternRange2).println(";")

        val loopVar = determineLoopVar()
        out.print("integer ").print(loopVar).println(";").println()

        out.println("initial begin")
        out.inc()
        out.println(outTmp.toString())

        out.println("for ($loopVar = 0; $loopVar < $lineCount; $loopVar = $loopVar + 1)")
        out.println("begin").inc()

        var rangeStart = rowBits - 1
        for (port in inputsInOrder) {
            val rangeEnd = rangeStart - port.bitWidth.width + 1
            val rangeStr = if (rangeStart != rangeEnd) {
                "[$rangeStart:$rangeEnd]"
            } else {
                "[$rangeStart]"
            }
            out.print(port.name).print(" = patterns[").print(loopVar).print("]").print(rangeStr).println(";")
            rangeStart -= port.bitWidth.width
        }
        out.println("#${params.waitTime};")

        for (port in outputsInOrder) {
            val dontCareValue = "${port.bitWidth.width}'hx"
            val rangeEnd = rangeStart - port.bitWidth.width + 1
            val rangeStr = if (rangeStart != rangeEnd) {
                "[$rangeStart:$rangeEnd]"
            } else {
                "[$rangeStart]"
            }
            out.print("if (patterns[").print(loopVar).print("]").print(rangeStr).print(" !== ").print(dontCareValue).println(")").println("begin")
            out.inc()
            out.print("if (").print(port.name).print(" !== patterns[").print(loopVar).print("]").print(rangeStr).println(")").println("begin")
            out.inc()
            out.print("\$display(\"%d:")
                .print(port.name).print(": Assertion failed, expected %h, actual is %h\", ")
                .print(loopVar).print(", ").print("patterns[").print(loopVar).print("]").print(rangeStr).print(", ")
                .print(port.name).print(");")
                .println()
            out.println("\$finish;")
            out.dec().println("end")
            out.dec().println("end")

            rangeStart -= port.bitWidth.width
        }

        out.dec()
        out.println("end")
        out.println().println("\$display(\"All tests passed.\");")

        out.dec()
        out.println("end")
    }

    private fun determineLoopVar(): String {
        var loopVar = "i"
        var lv = 0
        while (model.main.ports.any { it.name == loopVar }) {
            loopVar = "i" + (lv++)
        }
        return loopVar
    }

    private class VerilogTestVectorConsumer(
        out: CodePrinter,
        portOrder: List<HDLPort>,
        private val rowBits: Int
    ) : HdlTestVectorConsumer(out, portOrder) {

        override fun printValues(values: List<Value>, isClock: Boolean, clock: DigitalSignal?) {
            out.print("patterns[").print(line).print("] = ").print(rowBits).print("'b")

            values.forEachIndexed { index, value ->
                val port = portOrder[index]
                if (port.direction == HDLPort.Direction.OUT) {
                    if (clock != null && value.type == Value.Type.CLOCKED) {
                        out.print(value(clock.getValue(), port.bitWidth))
                    } else {
                        out.print(toBinaryString(value, port.bitWidth.width))
                    }
                    out.print("_")
                }
            }

            val sep = Separator(out, "_")

            values.forEachIndexed { index, value ->
                val port = portOrder[index]
                if (port.direction == HDLPort.Direction.IN) {
                    sep.check()
                    if (isClock) {
                        out.print(toBinaryString(Value.X, port.bitWidth.width))
                    } else {
                        out.print(toBinaryString(value, port.bitWidth.width))
                    }
                }
            }

            out.println(";")

            line++
        }

        override fun checkNewLine() {
            // empty
        }

        private fun toBinaryString(value: Value, bits: Int): String {
            var binStr = ""
            var fillChar = '0'
            when (value.type) {
                Value.Type.DONT_CARE -> fillChar = 'x'
                Value.Type.UNDEFINED -> fillChar = 'z'
                else -> {
                    if (bits > BitWidth.MAX) {
                        // TODO I18N
                        throw HDLException("Test vector longer than ${BitWidth.MAX} not supported")
                    }
                    binStr = BitOperation.longToBinaryPadded(value.value.getValue(), BitWidth.of(bits))
                }
            }

            val sb = StringBuilder()
            if (binStr.length < bits) {
                val diff = bits - binStr.length
                for (i in 0 until diff) {
                    sb.append(fillChar)
                }
            }
            sb.append(binStr)

            return sb.toString()
        }
    }
}