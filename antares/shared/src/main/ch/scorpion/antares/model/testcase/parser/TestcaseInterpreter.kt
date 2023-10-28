package ch.scorpion.antares.model.testcase.parser

import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.antares.model.testcase.TestVectorConsumer
import ch.scorpion.jabbah.base.dsl.AbstractBaseInterpreter
import ch.scorpion.antares.model.testcase.TestVector
import ch.scorpion.antares.model.testcase.Value
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.dsl.Node
import ch.scorpion.jabbah.graph.model.GraphPortOwner

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
	}

	override fun interpret(node: Node): Any {
		return when (node) {
			is TestScript -> testScript(node)
			else -> super.interpret(node)
		}
	}

	private fun testScript(testScript: TestScript): Any {
		val portNames: List<String> = testScript.portNames.names.map { it.value!! }
		for (statement in testScript.children) {
			when (statement) {
				is RunNode -> run(statement, portNames)
				is TestVectorNode -> testVector(0, TestVector.Type.Top, portNames, statement, mutableListOf())
			}
		}
		return 0L
	}

	private fun run(node: RunNode, portNames: List<String>) {
		if (node.children.size == 1) {
			testVector(0, TestVector.Type.Top, portNames, node.children.first(), mutableListOf())
		} else {
			node.children.forEachIndexed { index, child ->
				val type = when (index) {
					0 -> TestVector.Type.RunFirst
					node.children.size - 1 -> TestVector.Type.RunLast
					else -> TestVector.Type.RunLine
				}
				testVector(0, type, portNames, child, mutableListOf())
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
		portNames: List<String>,
		testVectorNode: TestVectorNode,
		testVectorValues: MutableList<Value>
	) {
		if (index == portNames.size) {
			consumer.consume(TestVector(type, "$LINE_TEXT ${testVectorNode.location.row}", testVectorValues.toTypedArray()))
			return
		}
		if (graphPortOwner.getGraphPort<DigitalSignal>(portNames[index])?.portType?.isInput == true
			&& testVectorNode.values[index].value.type == Value.Type.DONT_CARE
		) {
			val copy = copyValues(testVectorValues)
			testVectorValues.add(Value(DigitalSignalFactory.of(Bit.False)))
			testVector(index + 1, type, portNames, testVectorNode, testVectorValues)
			copy.add(Value(DigitalSignalFactory.of(Bit.True)))
			testVector(index + 1, type, portNames, testVectorNode, copy)
		} else {
			testVectorValues.add(testVectorNode.values[index].value)
			testVector(index + 1, type, portNames, testVectorNode, testVectorValues)
		}
	}

	private fun copyValues(l: MutableList<Value>): MutableList<Value> =
		l.map { it.doClone() }.toMutableList()
}