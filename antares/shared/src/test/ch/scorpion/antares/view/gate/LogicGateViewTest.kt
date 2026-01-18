package ch.scorpion.antares.view.gate

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.jabbah.edit.Look.SCALE
import ch.scorpion.antares.view.gate.BoxGateView.Companion.BIG_PORT_DISTANCE
import ch.scorpion.antares.view.port.AbstractAntaresPortView.Companion.LENGTH
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import kotlin.test.Test
import kotlin.test.assertEquals

class LogicGateViewTest {

	init {
		AntaresTestRule.configure()
	}

	@Test
	fun shouldCalculateUnconnectedPortConnectionPoint() {
		val andGateView = LogicGateView.andGateView()
		andGateView.location = Point2D(100, 100)
		andGateView.orientation = Direction.EAST

		val portView = andGateView.getPortView(andGateView.model.getPort(1))!!
		assertEquals(
			Point2D(100 - 2 * LENGTH - 6 * SCALE, 100 - BIG_PORT_DISTANCE * SCALE / 2),
			andGateView.getUnconnectedPortConnectionPoint(portView.port)
		)
	}

	@Test
	fun shouldCalculateRotatedUnconnectedPortConnectionPoint() {
		val andGateView = LogicGateView.andGateView()
		andGateView.location = Point2D(100, 100)
		andGateView.orientation = Direction.NORTH

		val portView = andGateView.getPortView(andGateView.model.getPort(1))!!
		assertEquals(
			Point2D(100 -BIG_PORT_DISTANCE * SCALE / 2, 100 + 2 * LENGTH + 6 * SCALE),
			andGateView.getUnconnectedPortConnectionPoint(portView.port)
		)
	}
}