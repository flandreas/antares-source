package ch.scorpion.jabbah.graph.view.port

import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rotation
import ch.scorpion.jabbah.edit.SnappableX
import ch.scorpion.jabbah.edit.SnappableY
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.model.port.PortImpl
import ch.scorpion.jabbah.graph.model.port.SubGraphPortImpl
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.VerticeView
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

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

	@Test
	fun shouldAcceptSnappableXPointingTowards() {
		val pv1 = portView(PortType.OUTPUT, Direction.SOUTH, Point2D(0, 0))
		val pv2 = portView(PortType.INPUT, Direction.NORTH, Point2D(0, 100))

		assertTrue(pv1.accept(pv2 as SnappableX))
	}

	@Test
	fun shouldNotAcceptSnappableXPointingAway() {
		val pv1 = portView(PortType.OUTPUT, Direction.SOUTH, Point2D(0, 100))
		val pv2 = portView(PortType.INPUT, Direction.NORTH, Point2D(0, 0))

		assertFalse(pv1.accept(pv2 as SnappableX))
	}

	@Test
	fun shouldAcceptSnappableXVerticallyAligned() {
		val pv1 = portView(PortType.OUTPUT, Direction.EAST, Point2D(0, 0))
		val pv2 = portView(PortType.OUTPUT, Direction.EAST, Point2D(0, 100))

		assertTrue(pv1.accept(pv2 as SnappableX))
	}

	@Test
	fun shouldNotAcceptSnappableXVerticallyUnaligned() {
		val pv1 = portView(PortType.OUTPUT, Direction.EAST, Point2D(0, 0))
		val pv2 = portView(PortType.OUTPUT, Direction.EAST, Point2D(10, 100))

		assertFalse(pv1.accept(pv2 as SnappableX))
	}

	@Test
	fun shouldAcceptSnappableYPointingTowards() {
		val pv1 = portView(PortType.OUTPUT, Direction.EAST, Point2D(0, 0))
		val pv2 = portView(PortType.INPUT, Direction.WEST, Point2D(100, 0))

		assertTrue(pv1.accept(pv2 as SnappableY))
	}

	@Test
	fun shouldNotAcceptSnappableYPointingAway() {
		val pv1 = portView(PortType.OUTPUT, Direction.EAST, Point2D(100, 0))
		val pv2 = portView(PortType.INPUT, Direction.WEST, Point2D(0, 0))

		assertFalse(pv1.accept(pv2 as SnappableY))
	}

	@Test
	fun shouldAcceptSnappableYHorizontallyAligned() {
		val pv1 = portView(PortType.OUTPUT, Direction.SOUTH, Point2D(0, 0))
		val pv2 = portView(PortType.OUTPUT, Direction.SOUTH, Point2D(100, 0))

		assertTrue(pv1.accept(pv2 as SnappableY))
	}

	@Test
	fun shouldNotAcceptSnappableYHorizontallyUnaligned() {
		val pv1 = portView(PortType.OUTPUT, Direction.SOUTH, Point2D(0, 0))
		val pv2 = portView(PortType.OUTPUT, Direction.SOUTH, Point2D(100, 10))

		assertFalse(pv1.accept(pv2 as SnappableY))
	}

	private fun portView(portType: PortType, direction: Direction, globalLocation: Point2D): TestPortView<*> {
		val portView = TestPortView(PortImpl(portType), direction)
		val owner = mockk<VerticeView<*>>()
		every { owner.getPortConnectionPoint(any()) } returns globalLocation
		every { owner.rotation } returns Rotation.R0
		portView.owner = owner
		return portView
	}
}