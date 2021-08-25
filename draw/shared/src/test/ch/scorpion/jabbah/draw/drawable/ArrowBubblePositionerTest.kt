package ch.scorpion.jabbah.draw.drawable

import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.draw.DrawTestRule
import ch.scorpion.jabbah.draw.View
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlin.test.*

class ArrowBubblePositionerTest {

	private val content = createContent(Dimension2D(200, 200))

	@BeforeTest
	fun beforeTest() {
		DrawTestRule.configure()
	}

	@Test
	fun shouldPositionBelowRightByDefault() {
		val view = createView(Dimension2D(1_000, 1_000))
		val describable = Rectangle2D.withCenter(Point2D(500, 500), 100.0, 100.0)

		val position: ArrowBubblePosition = ArrowBubblePositioner.position(content, describable, view)

		assertTrue(position.belowLocation)
		assertTrue(position.rightOfLocation)
		assertEquals(Point2D(500, 550 + ArrowBubblePositioner.DISTANCE), position.location)
	}

	@Test
	fun shouldPositionAboveRightNearBottomViewBorder() {
		val view = createView(Dimension2D(1_000, 1_000))
		val describable = Rectangle2D.withCenter(Point2D(500, 900), 100.0, 100.0)

		val position: ArrowBubblePosition = ArrowBubblePositioner.position(content, describable, view)

		assertFalse(position.belowLocation)
		assertTrue(position.rightOfLocation)
		assertEquals(Point2D(500, 850 - ArrowBubblePositioner.DISTANCE), position.location)
	}

	@Test
	fun shouldPositionLeftNearRightBorder() {
		val view = createView(Dimension2D(1_000, 1_000))
		val describable = Rectangle2D.withCenter(Point2D(900, 500), 100.0, 100.0)

		val position: ArrowBubblePosition = ArrowBubblePositioner.position(content, describable, view)

		assertTrue(position.belowLocation)
		assertFalse(position.rightOfLocation)
		assertEquals(Point2D(900, 550 + ArrowBubblePositioner.DISTANCE), position.location)
	}

	private fun createView(dimension: Dimension2D, zoom: Double = 1.0): View<*> {
		val view = mockk<View<*>>()
		val slotX = slot<Double>()
		val slotY = slot<Double>()
		val slotPoint = slot<Point2D>()
		every { view.width } returns dimension.width.toInt()
		every { view.height } returns dimension.height.toInt()
		every { view.modelToViewX(capture(slotX)) } answers { slotX.captured / zoom }
		every { view.modelToViewY(capture(slotY)) } answers { slotY.captured / zoom }
		every { view.modelToView(capture(slotPoint)) } answers { slotPoint.captured.multiply(zoom) }
		return view
	}

	private fun createContent(dimension: Dimension2D): RectangularDrawable {
		val content = mockk<RectangularDrawable>()
		every { content.width } returns dimension.width
		every { content.height } returns dimension.height
		return content
	}
}