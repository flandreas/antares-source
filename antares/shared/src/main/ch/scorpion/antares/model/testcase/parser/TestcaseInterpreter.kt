package ch.scorpion.antares.model.testcase.parser

import ch.scorpion.antares.model.testcase.TestVectorConsumer
import ch.scorpion.jabbah.base.dsl.AbstractBaseInterpreter
import ch.scorpion.antares.model.testcase.TestVector
import ch.scorpion.antares.model.testcase.Value
import ch.scorpion.jabbah.base.dsl.Node

/**
 * Interprets a [TestScript] AST by transforming them into [TestVector]s and
 * forwarding them to the provided [TestVectorConsumer].
 */
class TestcaseInterpreter(
	testScript: TestScript,
	private val consumer: TestVectorConsumer
) : AbstractBaseInterpreter(testScript) {

	override fun interpret(node: Node): Any {
		return when (node) {
			is TestScript -> testScript(node)
			else -> super.interpret(node)
		}
	}

	private fun testScript(testScript: TestScript): Any {
		for (testVector in testScript.testVectors.children) {
			testVector(testVector)
		}
		return 0L
	}

	private fun testVector(testVectorNode: TestVectorNode) {
		consumer.consume(TestVector(testVectorNode.values.map { Value(it.value) }.toTypedArray()))
	}
}