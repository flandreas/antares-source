package io.antarescircuit.antares.hdl.verilog

import io.antarescircuit.antares.hdl.*
import io.antarescircuit.antares.hdl.expression.*
import io.antarescircuit.antares.hdl.vhdl.HDLException
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.UUID
import io.antarescircuit.jabbah.base.io.CodePrinter
import io.antarescircuit.jabbah.base.logger

/**
 * Prints a [HDLCircuit] and all referenced sub-circuit [HDLCircuit]s as Verilog (IEEE 1364-2005)
 * modules using the specified [CodePrinter].
 */
class VerilogCreator(
	private val out: CodePrinter,
	private val applyDelays: Boolean = true,
) {

	companion object {
		private val LOG by logger(VerilogCreator::class)
	}

	/** Loads and manages [VerilogTemplate]s.*/
	private val library = VerilogLibrary()

	/** Helps to ensure that every [HDLCircuitNode] definition is only printed once.*/
	private val printedCircuitNodes = mutableSetOf<UUID>()

	/** Maps names of already printed template modules to their code. */
	private val printedModules = mutableMapOf<String, String>()

	private var firstModule = true

	fun printCircuit(circuit: HDLCircuit) {
		// Referenced modules are printed first
		circuit.nodes.forEach { node ->
			if (node is HDLCircuitNode) {
				printCircuitNode(node)
			} else if (node is BuiltInNode) {
				printBuiltInNode(node)
			}
		}

		LOG.debug("Exporting '${circuit.elementName}'")

		startModule()
		printModuleHeader(circuit)
		out.inc()
		printWires(circuit)
		printNodes(circuit)
		printOutputs(circuit)
		out.dec().println("endmodule")
	}

	/** Separates modules by an empty line. */
	private fun startModule() {
		if (!firstModule) {
			out.println()
		}
		firstModule = false
	}

	/**
	 * Prints the template module of [node] unless an identical module was printed before.
	 * Modules with the same name but different code, e.g. unlabeled ROMs with different content,
	 * are numbered.
	 */
	private fun printBuiltInNode(node: BuiltInNode) {
		val module = library.getTemplate(node).getModule(node)
		var name = module.name
		var index = 1
		while (true) {
			val code = module.code.replaceFirst("module ${module.name}", "module $name")
			val printedCode = printedModules[name]
			if (printedCode == null) {
				startModule()
				out.print(code)
				printedModules[name] = code
				break
			} else if (printedCode == code) {
				break
			}
			name = "${module.name}_${index++}"
		}
		node.hdlEntityName = name
	}

	private fun printCircuitNode(circuitNode: HDLCircuitNode) {
		if (printedCircuitNodes.add(circuitNode.circuit.uuid)) {
			printCircuit(circuitNode.circuit)
		}
	}

	private fun printModuleHeader(circuit: HDLCircuit) {
		out.print("// ").println(circuit.name)
		out.print("module ").print(circuit.entityName)
		val ports = circuit.inputs.map { "input ${Verilog.range(it.bitWidth)}${it.name}" } +
			circuit.outputs.map { "output ${Verilog.range(it.bitWidth)}${it.name}" } +
			circuit.inOuts.map { "inout ${Verilog.range(it.bitWidth)}${it.name}" }
		if (ports.isEmpty()) {
			out.println(";")
		} else {
			out.println(" (").inc()
			out.println(ports.joinToString(",\n"))
			out.dec().println(");")
		}
	}

	private fun printWires(circuit: HDLCircuit) {
		circuit.nets.filter { it.needsVariable }.forEach {
			out.print("wire ").print(Verilog.range(it.bitWidth)).print(it.name).println(";")
		}
	}

	private fun printNodes(circuit: HDLCircuit) {
		circuit.nodes.forEachIndexed { index, node ->
			when (node) {
				is HDLNodeAssignment -> printAssignment(node)
				is HDLCircuitNode -> printInstance(node, index, emptyList())
				is BuiltInNode -> printInstance(node, index, library.getTemplate(node).getModule(node).parameters)
				is ManyToOneNode -> printManyToOne(node)
				is OneToManyNode -> printOneToMany(node)
				else -> throw HDLException(Translations.getString("antares.vhdl.elementNotSupported.error.txt", node::class.simpleName.toString()))
			}
		}
	}

	private fun printAssignment(node: HDLNodeAssignment) {
		node.targetNet?.let {
			out.print("assign ")
			if (applyDelays && node.expression.delay > 0) {
				out.print("#").print(node.expression.delay).print(" ")
			}
			out.print(it.name).print(" = ")
			printExpression(node.expression)
			out.println(";")
		}
	}

	private fun printExpression(expression: Expression) {
		when (expression) {
			is NetExpression -> out.print(expression.net.name)
			is NotExpression -> {
				out.print("~")
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
				val op = when (expression.operation) {
					OperationExpression.Operation.AND -> " & "
					OperationExpression.Operation.OR -> " | "
					OperationExpression.Operation.XOR -> " ^ "
				}
				out.print("(")
				expression.operands.forEachIndexed { index, operand ->
					if (index > 0) {
						out.print(op)
					}
					printExpression(operand)
				}
				out.print(")")
			}
			is ConstantExpression -> out.print(Verilog.value(expression.value))
			else -> throw HDLException(Translations.getString("antares.vhdl.expressionNotSupported.error.text", expression::class.simpleName.toString()))
		}
	}

	/** Prints a module instance using named port connections. */
	private fun printInstance(node: BuiltInNode, index: Int, parameters: List<VerilogTemplate.Parameter>) {
		out.print(node.hdlEntityName).print(" ")
		if (parameters.isNotEmpty()) {
			out.println("#(").inc()
			out.println(parameters.joinToString(",\n") { ".${it.name}(${it.value})" })
			out.dec().print(") ")
		}
		out.print("node").print(index).println(" (").inc()

		val connections = mutableListOf<String>()
		for (ia in node.inputAssignments) {
			connections.add(".${ia.name}(${expressionString(ia.expression)})")
		}
		for (port in node.outputs + node.inOuts) {
			port.net?.let { net -> connections.add(".${port.name}(${net.name})") }
		}
		if (connections.isNotEmpty()) {
			out.println(connections.joinToString(",\n"))
		}
		out.dec().println(");")
	}

	private fun expressionString(expression: Expression): String =
		if (expression is NetExpression) {
			expression.net.name
		} else {
			throw HDLException(Translations.getString("antares.vhdl.expressionNotSupported.error.text", expression::class.simpleName.toString()))
		}

	private fun printOutputs(circuit: HDLCircuit) {
		for (output in circuit.outputs) {
			output.net?.let { net ->
				if (net.needsVariable || net.isInput) {
					out.print("assign ").print(output.name).print(" = ").print(net.name).println(";")
				}
			}
		}
	}

	/** Prints a concentrator as concatenation, most significant input first. */
	private fun printManyToOne(node: ManyToOneNode) {
		node.targetSignal?.let { target ->
			out.print("assign ").print(target).print(" = {")
			node.reversed().forEachIndexed { index, input ->
				if (index > 0) {
					out.print(", ")
				}
				printExpression(input.expression)
			}
			out.println("};")
		}
	}

	/** Prints a splitter as bit or part selects of the source vector. */
	private fun printOneToMany(node: OneToManyNode) {
		node.sourceSignal?.let { source ->
			val narrowSideBitWidth = node.bitWidth.width / node.branchCount.count
			var lsb = 0
			for (output in node.outputs) {
				output.net?.let { net ->
					out.print("assign ").print(net.name).print(" = ").print(source)
					if (node.bitWidth.width > 1) {
						if (narrowSideBitWidth == 1) {
							out.print("[").print(lsb).print("]")
						} else {
							out.print("[").print(lsb + narrowSideBitWidth - 1).print(":").print(lsb).print("]")
						}
					}
					out.println(";")
				}
				lsb += narrowSideBitWidth
			}
		}
	}
}
