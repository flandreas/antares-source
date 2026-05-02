package io.antarescircuit.antares.view.signal

import io.antarescircuit.antares.AntaresTestRule
import io.antarescircuit.antares.view.inout.DigitalCircuitInOutView
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.graph.container.ControlViewComponent
import io.antarescircuit.jabbah.graph.model.Vertice
import io.antarescircuit.jabbah.graph.view.ControlViewSource
import kotlin.test.Test
import kotlin.test.assertEquals

internal class DigitalSignalSourceControlViewTest {

	init {
		AntaresTestRule.configure()
	}

	@Test
	fun shouldMaintainLocationWhenClonedInControlViewComponent() {
		val inOutView = DigitalCircuitInOutView()
		@Suppress("UNCHECKED_CAST")
		val component = ControlViewComponent(source = inOutView as ControlViewSource<Vertice>)
		component.location = Point2D(100, 50)

		val clone = component.doClone() as ControlViewComponent

		assertEquals(Point2D(100, 50), clone.location)
		assertEquals(
			(component.controlView as DigitalSignalSourceControlView<*>).numberView!!.location,
			(clone.controlView as DigitalSignalSourceControlView<*>).numberView!!.location)
	}
}