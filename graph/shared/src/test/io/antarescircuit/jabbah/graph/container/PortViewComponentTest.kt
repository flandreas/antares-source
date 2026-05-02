package io.antarescircuit.jabbah.graph.container

import io.antarescircuit.jabbah.graph.model.PortType
import io.antarescircuit.jabbah.graph.model.port.SubGraphPortImpl
import io.antarescircuit.jabbah.graph.view.GraphViewTestRule
import io.antarescircuit.jabbah.graph.view.port.TestPortView
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

		val clone = component.doClone() as PortViewComponent

		assertEquals("test", clone.portView!!.port.name)
	}
}