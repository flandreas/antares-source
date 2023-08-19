package ch.scorpion.antares.hdl.vhdl

import ch.scorpion.antares.hdl.*
import ch.scorpion.antares.hdl.HDLPort.Direction.*
import ch.scorpion.antares.hdl.expression.*
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.io.CodePrinter
import ch.scorpion.jabbah.base.io.Separator
import ch.scorpion.jabbah.base.logger

/**
 * Prints a [HDLCircuit] and all referenced sub-circuit [HDLCircuit]s as VHDL
 * using the specified [CodePrinter].
 */
class VHDLCreator(private val out: CodePrinter) {

	companion object {
		private val LOG by logger(VHDLCreator::class)

		private fun value(constant: ConstantExpression): String = value(constant.value)

		private fun value(value: DigitalSignal): String =
			if (value.bitWidth.width > 1) {
				"\"${value.binaryString}\""
			} else {
				"\'${value.binaryString}\'"
			}
	}

	/** Helps to ensure that every [HDLCircuitNode] definition is only printed once.*/
	private val printedCircuitNodes = mutableSetOf<UUID>()

	fun printCircuit(circuit: HDLCircuit) {
		// Referenced entities must be printed first
		circuit.circuitNodes.forEachIndexed { index, node -> printCircuitNode(node, index == 0) }

		LOG.debug("Exporting '${circuit.elementName}'")

		if (circuit.circuitNodes.isNotEmpty()) {
			out.println()
		}

		printImports()
		printEntity(circuit)
		printBehaviour(circuit)
	}

	private fun printCircuitNode(circuitNode: HDLCircuitNode, first: Boolean) {
		if (!printedCircuitNodes.contains(circuitNode.circuit.uuid)) {
			if (!first) {
				out.println()
			}
			printCircuit(circuitNode.circuit)
			printedCircuitNodes.add(circuitNode.circuit.uuid)
		}
	}

	private fun printImports() {
		out
			.println("LIBRARY ieee;")
			.println("USE ieee.std_logic_1164.all;")
			.println("USE ieee.numeric_std.all;")
			.println()
	}

	private fun printEntity(circuit: HDLCircuit) {
		out.println("-- ${circuit.name}")
		out.print("entity ").print(circuit.elementName).println(" is").inc()
		writeEntityPorts(circuit)
		out.dec()
		out.print("end ").print(circuit.elementName).println(";")
		out.println()
	}

	private fun writeEntityPorts(circuit: HDLCircuit) {
		var count = 0
		out.println("port (").inc()
		circuit.inputs.forEach {
			count++
			writePort(it, count == circuit.portsCount)
		}
		circuit.outputs.forEach {
			count++
			writePort(it, count == circuit.portsCount)
		}
		circuit.inOuts.forEach {
			count++
			writePort(it, count == circuit.portsCount)
		}
		out.println(");").dec()
	}

	private fun writePort(port: HDLPort, isLast: Boolean) {
		out.print(port.name).print(": ").print(getOutsidePortDirection(port)).print(' ').print(getType(port.bitWidth))
		if (!isLast) {
			out.println(";")
		}
	}

	private fun getOutsidePortDirection(port: HDLPort): String =
		when (port.direction) {
			IN -> "out"
			OUT -> "in"
			INOUT -> "inout"
		}

	private fun getType(bitWidth: BitWidth): String {
		// TODO multi-bit
		return "std_logic"
	}

	private fun printBehaviour(circuit: HDLCircuit) {
		out.print("architecture Behavioral of ").print(circuit.elementName).println(" is").inc()
		writeSignals(circuit)
		out.dec().println("begin").inc()
		writeNodes(circuit)
		writeOutputs(circuit)
		out.dec().println("end Behavioral;")
	}

	private fun writeSignals(circuit: HDLCircuit) {
		circuit.nets.filter { it.needsVariable }.forEach {
			out.print("signal ").print(it.name).print(": ").print(getType(it.bitWidth)).println(";")
		}
	}

	private fun writeNodes(circuit: HDLCircuit) {
		circuit.nodes.forEachIndexed { index, node ->
			when (node) {
				is HDLNodeAssignment -> writeExpression(node)
				is HDLCircuitNode -> writeEntityInstantiation(node, index)
				else -> throw HDLException("HDL element ${node::class.simpleName} not yet implemented")
			}
		}
	}

	private fun writeExpression(node: HDLNodeAssignment) {
		node.targetNet?.let {
			out.print(it.name).print(" <= ")
			writeExpression(node.expression)
			out.println(";")
		}
	}

	private fun writeExpression(expression: Expression) {
		when (expression) {
			is NetExpression -> out.print(expression.net.name)
			is NotExpression -> {
				out.print("NOT ")
				val inner = expression.expression
				if (inner is NotExpression) {
					out.print("(")
					writeExpression(inner)
					out.print(")")
				} else {
					writeExpression(inner)
				}
			}
			is OperationExpression -> {
				out.print("(")
				var first = true
				val op = when (expression.operation) {
					OperationExpression.Operation.AND -> " AND "
					OperationExpression.Operation.OR -> " OR "
					OperationExpression.Operation.XOR -> " XOR "
					else -> throw HDLException("Unknown operation ${expression.operation}")
				}
				for (exp in expression.operands) {
					if (first) {
						first = false
					} else {
						out.print(op)
					}
					writeExpression(exp)
				}
				out.print(")")
			}
			is ConstantExpression -> {
				out.print(value(expression))
			}
			else -> TODO()
		}
	}

	private fun writeEntityInstantiation(node: HDLCircuitNode, index: Int) {
		out.print("node").print(index).print(": entity work.").print(node.circuit.uuid.id)
		out.println().inc()
		out.println("port map (").inc()
		val sep = Separator(out, ",\n")
		for (ia in node.inputAssignments) {
			sep.check()
			out.print(ia.name).print(" => ")
			writeExpression(ia.expression)
		}

		for (output in node.outputs) {
			output.net?.let { net ->
				sep.check()
				out.print(output.name).print(" => ").print(net.name)
			}
		}

		out.println(");").dec().dec()
	}

	private fun writeOutputs(circuit: HDLCircuit) {
		for (output in circuit.outputs) {
			output.net?.let { net ->
				if (net.needsVariable || net.isInput) {
					out.print(output.name).print(" <= ").print(net.name).println(";")
				}
			}
		}
	}
}