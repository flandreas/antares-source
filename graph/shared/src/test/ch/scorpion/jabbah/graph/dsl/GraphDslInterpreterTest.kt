package ch.scorpion.jabbah.graph.dsl

import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import kotlin.test.Test
import kotlin.test.assertEquals

class GraphDslInterpreterTest {

	companion object {
		init {
			GraphViewTestRule.configure()
		}
	}

	@Test
	fun shouldInterpretInitStatement() {
		val result = GraphDslInterpreter("""
			a = 0
			init {
				a = 42
			}
			a
		""".trimIndent()).interpret()
		assertEquals(42L, result)
	}
}