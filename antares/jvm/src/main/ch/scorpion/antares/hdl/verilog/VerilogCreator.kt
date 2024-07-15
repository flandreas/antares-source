package ch.scorpion.antares.hdl.verilog

import ch.scorpion.antares.hdl.HDLCircuit
import ch.scorpion.antares.hdl.HDLNodeAssignment
import ch.scorpion.antares.hdl.HDLPort
import ch.scorpion.antares.hdl.expression.*
import ch.scorpion.antares.model.signal.BitOperation
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.jabbah.base.io.CodePrinter
import ch.scorpion.jabbah.base.io.Separator
import ch.scorpion.jabbah.base.logger

class VerilogCreator(
    private val out: CodePrinter,
    private val applyDelays: Boolean = true
) {
    companion object {
        private val LOG by logger(VerilogCreator::class)
    }

    private var firstEntity = true

    fun printCircuit(circuit: HDLCircuit) {
        LOG.debug("Exporting '${circuit.elementName}'")

        if (!firstEntity) {
            out.println()
        }

        printModule(circuit)
    }

    private fun printModule(circuit: HDLCircuit) {
        out.print("module ").print(circuit.entityName).println(" (").inc()
        writeEntityPorts(circuit)
        out.dec().println().println(");")

        out.inc()
        printWires(circuit)
        printNodes(circuit)
        printBehaviour(circuit)

        out.dec().print("endmodule")
    }

    private fun writeEntityPorts(circuit: HDLCircuit) {
        val sep = Separator(out, ",\n")

        for (input in circuit.inputs) {
            sep.check()
            out.print(getType(input, input.bitWidth)).print(' ').print(input.name)
        }
        for (output in circuit.outputs) {
            sep.check()
            out.print(getType(output, output.bitWidth)).print(' ').print(output.name)
        }
    }

    private fun getType(port: HDLPort, bitWidth: BitWidth): String {
        val builder = StringBuilder()
        builder.append(getOutsidePortDirection(port))
        if (bitWidth.width > 1) {
            builder.append(" [${bitWidth.width - 1}:0")
        }
        return builder.toString()
    }

    private fun getOutsidePortDirection(port: HDLPort): String =
        when (port.direction) {
            HDLPort.Direction.IN -> "output"
            HDLPort.Direction.OUT -> "input"
            HDLPort.Direction.INOUT -> "input"
        }

    private fun printWires(circuit: HDLCircuit) {
        circuit.nets.filter { it.needsVariable }.forEach { net ->
            var range = ""
            if (net.bitWidth.width > 1) {
                range += " [${net.bitWidth.width - 1}:0]"
            }
            out.print("wire").print(range).print(' ').print(net.name).println(";")
        }
    }

    private fun printNodes(circuit: HDLCircuit) {
        circuit.nodes.forEachIndexed { index, node ->
            when (node) {
                is HDLNodeAssignment -> printExpression(node)
                else -> throw IllegalArgumentException("Unknown node type ${node.javaClass.canonicalName}")
            }
        }
    }

    private fun printBehaviour(circuit: HDLCircuit) {

    }

    private fun printExpression(node: HDLNodeAssignment) {
        node.targetNet?.let {
            out.print("assign ").print(it.name).print(" = ")
            printExpression(node.expression)
            out.println(";")
        }
    }

    private fun printExpression(expression: Expression) {
        when (expression) {
            is NetExpression -> out.print(expression.net.name)
            is NotExpression -> {
                out.print("~ ")
                val inner = expression.expression
                if (inner is NotExpression) {
                    out.print("(")
                    printExpression(inner)
                    out.print(")")
                } else {
                    printExpression(inner)
                }
            }
            is OperationExpression -> {
                out.print("(")
                var first = true
                val op = when (expression.operation) {
                    OperationExpression.Operation.AND -> " & "
                    OperationExpression.Operation.OR -> " | "
                    OperationExpression.Operation.XOR -> " ^ "
                }
                for (exp in expression.operands) {
                    if (first) {
                        first = false
                    } else {
                        out.print(op)
                    }
                    printExpression(exp)
                }
                out.print(")")
            }
            is ConstantExpression -> {
                out.print(value(expression))
            }
        }
    }

    private fun value(constant: ConstantExpression): String =
        value(constant.value.getValue(), constant.value.bitWidth)

    private fun value(value: ULong, bitWidth: BitWidth): String {
        var s = BitOperation.longToBinaryPadded(value, bitWidth)
        return "${bitWidth.width}b$s"
    }
}