package ch.scorpion.jabbah.graph.view.port

import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.model.port.SubGraphPortImpl
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import kotlin.test.Test
import kotlin.test.assertEquals

/** Unit tests for functionality in [AbstractPortView]. */
class AbstractPortViewTest {

	companion object {
		init {
			GraphViewTestRule.configure()
		}
	}

	@Test
	fun shouldCloneIncludingModel() {
		val portView = TestPortView<Boolean>(SubGraphPortImpl(PortType.OUTPUT, name = "test"))

		val clone = portView.doClone()

		assertEquals("test", clone.port.name)
	}
}