package io.antarescircuit.jabbah.base.dsl

import kotlin.test.Test
import kotlin.test.assertEquals

class DslGlobalFunctionsTest {

	@Test
	fun shouldCalculateLog2() {
		testLog2(2L, "log2(4)")
		testLog2(3L, "log2(8)")
		testLog2(4L, "log2(10)")
		testLog2(4L, "log2(16)")
	}

	private fun testLog2(expected: Long, expression: String) {
		val interpreter = Interpreter(expression)
		val result = interpreter.interpret()
		assertEquals(expected, result)
	}
}