package io.antarescircuit.jabbah.graph.view.port

import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.geom.Rotation
import io.antarescircuit.jabbah.draw.InputEventContext
import io.antarescircuit.jabbah.edit.SnappableX
import io.antarescircuit.jabbah.edit.SnappableY
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.graph.model.PortType
import io.antarescircuit.jabbah.graph.model.port.PortImpl
import io.antarescircuit.jabbah.graph.model.port.SubGraphPortImpl
import io.antarescircuit.jabbah.graph.view.GraphViewTestRule
import io.antarescircuit.jabbah.graph.view.VerticeView
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Unit tests for functionality in [AbstractPortView]. */
class AbstractPortViewTest {

	init {
		GraphViewTestRule.configure()
	}

	private fun portView(
		portType: PortType,
		direction: Direction = Direction.EAST,
		globalLocation: Point2D = Point2D.ZERO,
		name: String? = null,
		desc: TranslatableText = TranslatableText()
	): TestPortView<*> {
		val portView = TestPortView(PortImpl(portType, name, desc), direction)
		val owner = mock<VerticeView<*>>()
		every { owner.getUnconnectedPortConnectionPoint(any()) } returns globalLocation
		every { owner.getPortConnectionPoint(any())} returns Point2D.ZERO
		every { owner.rotation } returns Rotation.R0
		portView.owner = owner
		return portView
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

	private fun tooltipText(portView: TestPortView<*>): String? = portView.getTooltip(InputEventContext(mock()))?.text

	@Test
	fun testSimpleInputTooltip() {
		assertEquals("*(Input) \nPort ID: 0", tooltipText(portView(PortType.INPUT)))
	}

	@Test
	fun testSimpleOutputTooltip() {
		val portView = portView(PortType.OUTPUT)
		assertEquals("*(Output) \nPort ID: 0", tooltipText(portView))
	}

	@Test
	fun testNamedInputTooltip() {
		assertEquals("*(Input 'Bla') \nPort ID: 0", tooltipText(portView(PortType.INPUT, name = "Bla")))
	}

	@Test
	fun testNamedDescInputTooltip() {
		assertEquals("*(Input 'Bla':) Desc\nPort ID: 0", tooltipText(portView(PortType.INPUT, name = "Bla", desc = TranslatableText("Desc"))))
	}

	@Test
	fun testUnnamedDescInputTooltip() {
		assertEquals("*(Input:) Desc\nPort ID: 0", tooltipText(portView(PortType.INPUT, desc = TranslatableText("Desc"))))
	}
}