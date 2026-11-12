package io.antarescircuit.antares.model.testcase.parser

import io.antarescircuit.antares.model.DigitalGraph
import io.antarescircuit.antares.model.net.Probe
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.model.testcase.Value
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.dsl.Node
import io.antarescircuit.jabbah.base.dsl.SemanticAnalyser
import io.antarescircuit.jabbah.base.dsl.SemanticError
import io.antarescircuit.jabbah.base.parser.TextLocation
import io.antarescircuit.jabbah.graph.model.GraphPort
import kotlin.math.min

class TestcaseAnalyser(
	private val circuit: DigitalGraph
) : SemanticAnalyser {

	companion object {
		/** The maximum number of test vectors a test script may expand to (through loops and X inputs).*/
		const val MAX_TEST_VECTORS = 1_000_000L

		/** Saturation value for counting test vectors without risking overflows.*/
		private const val TOO_MANY = MAX_TEST_VECTORS + 1
	}

	private lateinit var portNames: List<String>

	override fun analyse(program: Node) {
		val script = program as TestScript
		portNames = script.portNames.names.map { it.name.value!! }

		ensurePortsExistAndIsUnique(portNames)
		ensureAtLeastOneInput(portNames)
		ensureAtLeastOneOutput(portNames)
		if (statements(script.children, false, emptySet()) > MAX_TEST_VECTORS) {
			throwSemanticError("antares.testcase.error.tooManyTestVectors", MAX_TEST_VECTORS)
		}
	}

	private fun throwSemanticError(key: String, vararg params: Any) {
		throw SemanticError(TextLocation.UNDEFINED, Translations.getString(key, *params))
	}

	/**
	 * Checks the statements in [nodes] recursively and returns the number of test vectors they expand to,
	 * saturated at [TOO_MANY].
	 *
	 * @param inRun whether the statements are contained in a "run" block
	 * @param variables the loop variables defined for these statements
	 */
	private fun statements(nodes: List<Node>, inRun: Boolean, variables: Set<String>): Long {
		var count = 0L
		for (node in nodes) {
			val nodeCount = when (node) {
				is RunNode -> statements(node.children, true, variables)
				is LoopNode -> {
					val iterations = node.iterationCount?.takeIf { it < TOO_MANY.toULong() }?.toLong() ?: TOO_MANY
					// An empty body still costs one interpreter step per iteration
					val bodyCount = statements(node.children, inRun, variables + node.variable)
					if (bodyCount == 0L) iterations else iterations * bodyCount
				}
				is TestVectorNode -> testVector(node, inRun, variables)
				else -> 0L
			}
			count = min(count + nodeCount, TOO_MANY)
		}
		return count
	}

	/** Checks [node] and returns the number of test vectors it expands to due to X inputs, saturated at [TOO_MANY].*/
	private fun testVector(node: TestVectorNode, inRun: Boolean, variables: Set<String>): Long {
		ensureValueCount(portNames.size, node)
		if (inRun) {
			ensureNoDontCareInRunBlock(node)
		} else {
			ensureAtMostOneClock(node)
		}
		node.values.filterIsInstance<ExpressionValueNode>().forEach { ensureVariablesDefined(it.expression, variables) }

		val dontCareInputs = node.values.indices.count {
			node.values[it].type == Value.Type.DONT_CARE && circuit.getGraphPort<DigitalSignal>(portNames[it])?.portType?.isInput == true
		}
		return if (dontCareInputs >= 20) TOO_MANY else min(1L shl dontCareInputs, TOO_MANY)
	}

	private fun ensureVariablesDefined(expression: Expression, variables: Set<String>) {
		when (expression) {
			is NumberExpression -> {}
			is VariableExpression -> if (expression.name !in variables) {
				throwSemanticError("antares.testcase.error.unknownVariable", expression.name, expression.location.row)
			}
			is UnaryExpression -> ensureVariablesDefined(expression.operand, variables)
			is BinaryExpression -> {
				ensureVariablesDefined(expression.left, variables)
				ensureVariablesDefined(expression.right, variables)
			}
		}
	}

	private fun ensurePortsExistAndIsUnique(portNames: List<String>) {
		for (name in portNames) {
			val elements = circuit.elements.filter { it is GraphPort<*> && it.name == name || it is Probe && it.name == name }
			if (elements.isEmpty()) {
				throwSemanticError("antares.testcase.error.unknownPort", name)
			} else if (elements.size > 1) {
				throwSemanticError("antares.testcase.error.ambiguousOutputName", name)
			}
		}
	}

	private fun ensureAtLeastOneInput(portNames: List<String>) {
		val elements = circuit.elements.filter {
			it is GraphPort<*> && portNames.contains(it.name) && it.portType.isInput
		}
		if (elements.isEmpty()) {
			throwSemanticError("antares.testcase.error.noInput")
		}
	}

	private fun ensureAtLeastOneOutput(portNames: List<String>) {
		val elements = circuit.elements.filter {
			it is GraphPort<*> && portNames.contains(it.name) && it.portType.isOutput
				|| it is Probe && portNames.contains(it.name)
		}
		if (elements.isEmpty()) {
			throwSemanticError("antares.testcase.error.noOutput")
		}
	}

	private fun ensureValueCount(portNameCount: Int, testVector: TestVectorNode) {
		if (testVector.values.size > portNameCount) {
			throwSemanticError("antares.testcase.error.tooManyValues", testVector.location.row)
		}
		if (testVector.values.size < portNameCount) {
			throwSemanticError("antares.testcase.error.tooFewValues", testVector.location.row)
		}
	}

	private fun ensureNoDontCareInRunBlock(node: TestVectorNode) {
		if (node.values.any { it.type == Value.Type.DONT_CARE }) {
			throwSemanticError("antares.testcase.error.dontCareInRunBlock", node.location.row)
		}
	}

	private fun ensureAtMostOneClock(node: TestVectorNode) {
		if (node.values.count { it.type == Value.Type.CLOCKED } > 1) {
			throwSemanticError("antares.testcase.error.moreThanOneClock", node.location.row)
		}
	}
}
