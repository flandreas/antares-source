package ch.scorpion.antares.view.signal

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.view.inout.CircuitInOutView
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.graph.container.ControlViewComponent
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.view.ControlViewSource
import kotlin.test.Test
import kotlin.test.assertEquals

internal class DigitalSignalSourceControlViewTest {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	@Test
	fun shouldMaintainLocationWhenClonedInControlViewComponent() {
		val inOutView = CircuitInOutView()
		val component = ControlViewComponent(source = inOutView as ControlViewSource<Vertice>)
		component.location = Point2D(100, 50)

		val clone = component.doClone() as ControlViewComponent

		assertEquals(Point2D(100, 50), clone.location)
		assertEquals(
			(component.controlView as DigitalSignalSourceControlView<*>).numberView!!.location,
			(clone.controlView as DigitalSignalSourceControlView<*>).numberView!!.location)
	}
}