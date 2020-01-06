package ch.scorpion.jabbah.graph.model.vertice

import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GraphInputImplTest {

	companion object {
		 init {
			 GraphViewTestRule.configure()
		 }
	}

	@Test
	fun shouldApplyDefaultName() {
		val input = GraphInputImpl<Boolean>()
		assertEquals("I1", input.name)
	}

	@Test
	fun shouldRejectEmptyName() {
		val input = GraphInputImpl<Boolean>(name = "I")
		assertFailsWith<IllegalArgumentException> {
			input.name = ""
		}
		assertEquals("I", input.name)
	}
}