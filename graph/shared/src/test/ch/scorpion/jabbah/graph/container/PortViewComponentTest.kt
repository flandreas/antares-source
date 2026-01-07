package ch.scorpion.jabbah.graph.container

import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.model.port.SubGraphPortImpl
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.port.TestPortView
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class PortViewComponentTest {

	@BeforeTest
	fun setup() {
		GraphViewTestRule.configure()
	}

	@Test
	fun shouldClone() {
		val component = PortViewComponent(portView = TestPortView(SubGraphPortImpl(PortType.OUTPUT, "test")))

		val clone = component.doClone() as PortViewComponent<Boolean>

		assertEquals("test", clone.portView!!.port.name)
	}
}