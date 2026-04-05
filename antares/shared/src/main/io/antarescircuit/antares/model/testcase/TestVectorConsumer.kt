package io.antarescircuit.antares.model.testcase

import io.antarescircuit.antares.model.testcase.parser.TestcaseInterpreter
import io.antarescircuit.antares.model.testcase.parser.TestcaseParser
import io.antarescircuit.antares.model.testcase.parser.TestScript

/**
 * Consumes [TestVector]s produced by [TestcaseInterpreter] while interpreting a
 * [TestScript] produced by a [TestcaseParser].
 */
interface TestVectorConsumer {
	fun consume(testVector: TestVector)
}

/**
 * A [TestVectorCollector] that collects all consumed [TestVector] and provides
 * them for later access.
 */
class TestVectorCollector : TestVectorConsumer, Iterable<TestVector> {

	private val _testVectors = mutableListOf<TestVector>()
	val testVectors: List<TestVector> get() = _testVectors

	val size: Int get() = _testVectors.size

	fun get(index: Int): TestVector = _testVectors[index]

	override fun consume(testVector: TestVector) {
		_testVectors.add(testVector)
	}

	override fun iterator(): Iterator<TestVector> = _testVectors.iterator()
}