package ch.scorpion.antares.model.testcase.parser

import ch.scorpion.antares.model.signal.DigitalSignal
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
		for (testVector in testScript.testVectors.children) {
			testVector(0, portNames, testVector, mutableListOf())
		}
		return 0L
	}

	private fun testVector(
		index: Int,
		portNames: List<String>,
		testVectorNode: TestVectorNode,
		testVectorValues: MutableList<Value>
	) {
		if (index == portNames.size) {
			consumer.consume(TestVector("$LINE_TEXT ${testVectorNode.location.row}", testVectorValues.toTypedArray()))
			return
		}
		if (graphPortOwner.getGraphPort<DigitalSignal>(portNames[index])?.portType?.isInput == true
			&& testVectorNode.values[index].value.type == Value.Type.DONT_CARE
		) {
			val copy = copyValues(testVectorValues)
			testVectorValues.add(Value(0UL))
			testVector(index + 1, portNames, testVectorNode, testVectorValues)
			copy.add(Value(1UL))
			testVector(index + 1, portNames, testVectorNode, copy)
		} else {
			testVectorValues.add(testVectorNode.values[index].value)
			testVector(index + 1, portNames, testVectorNode, testVectorValues)
		}
	}

	private fun copyValues(l: MutableList<Value>): MutableList<Value> =
		l.map { it.doClone() }.toMutableList()
}