package io.antarescircuit.antares.hdl.vhdl

import io.antarescircuit.antares.hdl.*
import io.antarescircuit.antares.hdl.expression.*
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.UUID
import io.antarescircuit.jabbah.base.io.CodePrinter
import io.antarescircuit.jabbah.base.io.Separator
import io.antarescircuit.jabbah.base.logger

/**
 * Prints a [HDLCircuit] and all referenced sub-circuit [HDLCircuit]s as VHDL
 * using the specified [CodePrinter].
 */
class VHDLCreator(
	out: CodePrinter,
	private val applyDelays: Boolean = true
) : AbstractVHDLCreator(out) {

	companion object {
		private val LOG by logger(VHDLCreator::class)

		private fun value(constant: ConstantExpression): String = value(constant.value)
	}

	/** Loads and manages [VHDLTemplate]s.*/
	private val library = VHDLLibrary()

	/** Helps to ensure that every [HDLCircuitNode] definition is only printed once.*/
	private val printedCircuitNodes = mutableSetOf<UUID>()

	private var firstEntity = true

	fun printCircuit(circuit: HDLCircuit) {
		// Referenced entities must be printed first
		circuit.nodes.forEach { node ->
			if (node is HDLCircuitNode) {
				printCircuitNode(node)
			} else if (node is BuiltInNode) {
				printBuiltInNode(node)
			}
		}

		LOG.debug("Exporting '${circuit.elementName}'")

		if (!firstEntity) {
			out.println()
		}

		printImports()
		printEntity(circuit)
		printBehaviour(circuit)
	}

	private fun printBuiltInNode(builtInNode: BuiltInNode) {
		val template = library.getTemplate(builtInNode)
		if (!firstEntity) {
			out.println()
		}
		val entityName = template.print(out, builtInNode)
		firstEntity = false
		builtInNode.hdlEntityName = entityName
	}

	private fun printCircuitNode(circuitNode: HDLCircuitNode) {
		if (!printedCircuitNodes.contains(circuitNode.circuit.uuid)) {
			if (!firstEntity) {
				out.println()
			}
			printCircuit(circuitNode.circuit)
			printedCircuitNodes.add(circuitNode.circuit.uuid)
			firstEntity = false
		}
	}

	private fun printEntity(circuit: HDLCircuit) {
		out.println("-- ${circuit.name}")
		out.print("entity ").print(circuit.entityName).println(" is").inc()
		printEntityPorts(circuit)
		out.dec()
		out.print("end ").print(circuit.entityName).println(";")
		out.println()
	}

	private fun printBehaviour(circuit: HDLCircuit) {
		out.print("architecture Behavioral of ").print(circuit.entityName).println(" is").inc()
		printSignals(circuit)
		out.dec().println("begin").inc()
		printNodes(circuit)
		printOutputs(circuit)
		out.dec().println("end Behavioral;")
	}

	private fun printSignals(circuit: HDLCircuit) {
		circuit.nets.filter { it.needsVariable }.forEach {
			out.print("signal ").print(it.name).print(": ").print(getType(it.bitWidth)).println(";")
		}
	}

	private fun printNodes(circuit: HDLCircuit) {
		circuit.nodes.forEachIndexed { index, node ->
			when (node) {
				is HDLNodeAssignment -> printExpressions(node)
				is HDLCircuitNode -> printEntityInstantiation(node, index)
				is BuiltInNode -> printEntityInstantiation(node, index)
				is ManyToOneNode -> printManyToOne(node)
				is OneToManyNode -> printOneToMany(node)
				else -> throw HDLException(Translations.getString("antares.vhdl.elementNotSupported.error.txt", node::class.simpleName.toString()))
			}
		}
	}

	private fun printExpressions(node: HDLNodeAssignment) {
		node.targetNet?.let {
			out.print(it.name).print(" <= ")
			printExpression(node.expression)
			if (applyDelays && node.expression.delay > 0) {
				out.print(" after ").print(node.expression.delay).println(" ns;")
			} else {
				out.println(";")
			}
		}
	}

	private fun printExpression(expression: Expression) {
		when (expression) {
			is NetExpression -> out.print(expression.net.name)
			is NotExpression -> {
				out.print("NOT ")
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
					OperationExpression.Operation.AND -> " AND "
					OperationExpression.Operation.OR -> " OR "
					OperationExpression.Operation.XOR -> " XOR "
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
			else -> throw HDLException(Translations.getString("antares.vhdl.expressionNotSupported.error.text", expression::class.simpleName.toString()))
		}
	}

	private fun printEntityInstantiation(node: BuiltInNode, index: Int) {
		out.print("node").print(index).print(": entity work.").print(node.hdlEntityName)
		out.println().inc()
		if (node !is HDLCircuitNode) {
			library.getTemplate(node).writeGenericMap(out, node)
		}
		out.println("port map (").inc()
		val sep = Separator(out, ",\n")
		for (ia in node.inputAssignments) {
			sep.check()
			out.print(ia.name).print(" => ")
			printExpression(ia.expression)
		}

		for (output in node.outputs) {
			output.net?.let { net ->
				sep.check()
				out.print(output.name).print(" => ").print(net.name)
			}
		}

		for (inout in node.inOuts) {
			inout.net?.let { net ->
				sep.check()
				out.print(inout.name).print(" => ").print(net.name)
			}
		}

		out.println(");").dec().dec()
	}

	private fun printOutputs(circuit: HDLCircuit) {
		for (output in circuit.outputs) {
			output.net?.let { net ->
				if (net.needsVariable || net.isInput) {
					out.print(output.name).print(" <= ").print(net.name).println(";")
				}
			}
		}
	}

	private fun printManyToOne(node: ManyToOneNode) {
		node.targetSignal?.let { target ->
			for (input in node) {
				out.print(target).print("(")
				if (input.lsb == input.msb) {
					out.print(input.lsb)
				} else {
					out.print(input.msb).print(" downto ").print(input.lsb)
				}
				out.print(") <= ")
				printExpression(input.expression)
				out.println(";")
			}
		}
	}

	private fun printOneToMany(node: OneToManyNode) {
		node.sourceSignal?.let { source ->
			val narrowSideBitWidth = node.bitWidth.width / node.branchCount.count
			var i = 0
			for (output in node.outputs) {
				if (output.net != null) {
					out.print(output.net!!.name).print(" <= ").print(source).print("(")
					if (narrowSideBitWidth == 1) {
						out.print(i)
					} else {
						out.print(i + narrowSideBitWidth - 1).print(" downto ").print(i)
					}
					out.println(");")
				}
				i += narrowSideBitWidth
			}
		}
	}
}