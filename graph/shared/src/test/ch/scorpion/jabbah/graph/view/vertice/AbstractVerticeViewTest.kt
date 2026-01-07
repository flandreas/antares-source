package ch.scorpion.jabbah.graph.view.vertice

import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import kotlin.test.Test
import kotlin.test.assertEquals

/** Unit tests for functionality in [AbstractVerticeView].*/
class AbstractVerticeViewTest {

	init {
		GraphViewTestRule.configure()
	}

	@Test
	fun shouldCloneIncludingModel() {
		val vv = TestVerticeView("test")

		val clone = vv.doClone() as TestVerticeView

		assertEquals("test", clone.model.name)
	}
}