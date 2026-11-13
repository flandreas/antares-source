package io.antarescircuit.antares.model.testcase.parser

import io.antarescircuit.antares.model.inout.DigitalCircuitInOut
import io.antarescircuit.antares.model.signal.Bit
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.model.signal.DigitalSignalFactory
import io.antarescircuit.antares.model.testcase.TestVectorConsumer
import io.antarescircuit.jabbah.base.dsl.AbstractBaseInterpreter
import io.antarescircuit.antares.model.testcase.TestVector
import io.antarescircuit.antares.model.testcase.Value
import io.antarescircuit.antares.model.testcase.parser.TestcaseTokenType.*
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.dsl.Node
import io.antarescircuit.jabbah.base.dsl.SemanticError
import io.antarescircuit.jabbah.graph.model.GraphPortOwner

/** Maps loop variable names to their current values.*/
private typealias Variables = Map<String, ULong>

/**
 * Interprets a [TestScript] AST by transforming them into [TestVector]s and
 * forwarding them to the provided [TestVectorConsumer].
 */
class TestcaseInterpreter(
	testScript: TestScript,
	private val graphPortOwner: GraphPortOwner,
	private val consumer: TestVectorConsumer
) : AbstractBaseInterpreter(testScript) {

	companion object {
		private val LINE_TEXT = Translations.getString("antares.testcase.result.line.txt")
		private val MAX_BIT_WIDTH = BitWidth.of(BitWidth.MAX)
	}

	private lateinit var portNames: List<String>

	override fun interpret(node: Node): Any {
		return when (node) {
			is TestScript -> testScript(node)
			else -> super.interpret(node)
		}
	}

	private fun testScript(testScript: TestScript): Any {
		portNames = testScript.portNames.names.map { it.name.value!! }
		statements(testScript.children, emptyMap())
		return 0L
	}

	private fun statements(nodes: List<Node>, variables: Variables) {
		for (statement in nodes) {
			when (statement) {
				is RunNode -> run(statement, variables)
				is LoopNode -> statement.range.forEach { statements(statement.children, variables + (statement.variable to it)) }
				is TestVectorNode -> testVector(0, TestVector.Type.Top, statement, variables, mutableListOf())
			}
		}
	}

	private fun run(node: RunNode, variables: Variables) {
		val vectors = mutableListOf<Pair<TestVectorNode, Variables>>()
		collectRunVectors(node.children, variables, vectors)
		vectors.forEachIndexed { index, (vector, vectorVariables) ->
			val type = when {
				vectors.size == 1 -> TestVector.Type.Top
				index == 0 -> TestVector.Type.RunFirst
				index == vectors.size - 1 -> TestVector.Type.RunLast
				else -> TestVector.Type.RunLine
			}
			testVector(0, type, vector, vectorVariables, mutableListOf())
		}
	}

	/** Expands the loops of a run block into a flat list of test vectors and their variable values.*/
	private fun collectRunVectors(nodes: List<Node>, variables: Variables, result: MutableList<Pair<TestVectorNode, Variables>>) {
		for (node in nodes) {
			when (node) {
				is LoopNode -> node.range.forEach { collectRunVectors(node.children, variables + (node.variable to it), result) }
				is TestVectorNode -> result.add(node to variables)
			}
		}
	}

	/**
	 * Recursive method that duplicates every [TestVector] for every value containing [Value.Type.DONT_CARE].
	 * Traverses the values list from left to right, starting with [index], and adds the resulting
	 * [TestVector]s to [testVectorValues]. At the end, [consumer] is called for every final [TestVector].
	 */
	private fun testVector(
		index: Int,
		type: TestVector.Type,
		testVectorNode: TestVectorNode,
		variables: Variables,
		testVectorValues: MutableList<Value>
	) {
		if (index == portNames.size) {
			consumer.consume(TestVector(type, "$LINE_TEXT ${testVectorNode.location.row}", testVectorValues.toTypedArray()))
			return
		}
		val port = graphPortOwner.getGraphPort<DigitalSignal>(portNames[index])
		val valueNode = testVectorNode.values[index]
		if (port?.portType?.isInput == true && valueNode.type == Value.Type.DONT_CARE) {
			val copy = copyValues(testVectorValues)
			testVectorValues.add(Value(DigitalSignalFactory.of(Bit.False)))
			testVector(index + 1, type, testVectorNode, variables, testVectorValues)
			copy.add(Value(DigitalSignalFactory.of(Bit.True)))
			testVector(index + 1, type, testVectorNode, variables, copy)
		} else {
			val value = when (valueNode) {
				is LiteralValueNode -> valueNode.value
				is ExpressionValueNode -> {
					val bitWidth = (port as? DigitalCircuitInOut)?.bitWidth ?: MAX_BIT_WIDTH
					Value(DigitalSignalFactory.of(bitWidth, evaluate(valueNode.expression, variables)), valueNode.type)
				}
			}
			testVectorValues.add(value)
			testVector(index + 1, type, testVectorNode, variables, testVectorValues)
		}
	}

	/** Evaluates [expression] with unsigned 64 bit wrap-around arithmetic.*/
	private fun evaluate(expression: Expression, variables: Variables): ULong =
		when (expression) {
			is NumberExpression -> expression.value
			is VariableExpression -> variables[expression.name] ?: throw SemanticError(
				expression.location,
				Translations.getString("antares.testcase.error.unknownVariable", expression.name, expression.location.row)
			)
			is UnaryExpression -> {
				val operand = evaluate(expression.operand, variables)
				if (expression.operator == MINUS) 0UL - operand else operand.inv()
			}
			is BinaryExpression -> {
				val left = evaluate(expression.left, variables)
				val right = evaluate(expression.right, variables)
				when (expression.operator) {
					PLUS -> left + right
					MINUS -> left - right
					MULTIPLY -> left * right
					AMPERSAND -> left and right
					VERTICAL_BAR -> left or right
					CARET -> left xor right
					SHIFT_LEFT -> if (right >= 64UL) 0UL else left shl right.toInt()
					SHIFT_RIGHT -> if (right >= 64UL) 0UL else left shr right.toInt()
					else -> throw IllegalStateException("Unsupported operator ${expression.operator}")
				}
			}
		}

	private fun copyValues(l: MutableList<Value>): MutableList<Value> =
		l.map { it.doClone() }.toMutableList()
}
