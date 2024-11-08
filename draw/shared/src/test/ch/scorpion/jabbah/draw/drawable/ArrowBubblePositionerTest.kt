package ch.scorpion.jabbah.draw.drawable

import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.draw.CanvasMockBuilder
import ch.scorpion.jabbah.draw.DrawTestRule
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.draw.drawable.ArrowBubblePositioner.DISTANCE
import ch.scorpion.jabbah.draw.drawable.ArrowBubblePositioner.MIN_VIEW_DISTANCE
import dev.mokkery.MockMode
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.capture.Capture
import dev.mokkery.matcher.capture.capture
import dev.mokkery.matcher.capture.get
import dev.mokkery.mock
import kotlin.test.*

class ArrowBubblePositionerTest {

	@BeforeTest
	fun beforeTest() {
		DrawTestRule.configure()
	}

	@Test
	fun shouldPositionBelowRightByDefault() {
		val content = createContent(Dimension2D(200, 200))
		val view = createView(Dimension2D(1_000, 1_000))
		val describable = Rectangle2D.withCenter(Point2D(500, 500), 100.0, 100.0)

		val position: ArrowBubblePosition = ArrowBubblePositioner.position(content, describable, view, preferredBelow = true)

		assertTrue(position.belowLocation)
		assertTrue(position.rightOfLocation)
		assertEquals(Point2D(500, 550 + DISTANCE), position.location)
	}

	@Test
	fun shouldPositionAboveRightNearBottomViewBorder() {
		val content = createContent(Dimension2D(200, 200))
		val view = createView(Dimension2D(1_000, 1_000))
		val describable = Rectangle2D.withCenter(Point2D(500, 900), 100.0, 100.0)

		val position: ArrowBubblePosition = ArrowBubblePositioner.position(content, describable, view, preferredBelow = true)

		assertFalse(position.belowLocation)
		assertTrue(position.rightOfLocation)
		assertEquals(Point2D(500, 850 - DISTANCE), position.location)
	}

	@Test
	fun shouldPositionLeftNearRightBorder() {
		val content = createContent(Dimension2D(200, 200))
		val view = createView(Dimension2D(1_000, 1_000))
		val describable = Rectangle2D.withCenter(Point2D(900, 500), 100.0, 100.0)

		val position: ArrowBubblePosition = ArrowBubblePositioner.position(content, describable, view, preferredBelow = true)

		assertTrue(position.belowLocation)
		assertFalse(position.rightOfLocation)
		assertEquals(Point2D(900, 550 + DISTANCE), position.location)
	}

	@Test
	fun shouldFineTuneLeftNearRightBorder() {
		val content = createContent(Dimension2D(600, 200))
		val view = createView(Dimension2D(1_000, 1_000))
		val describable = Rectangle2D.withCenter(Point2D(550, 500), 100.0, 100.0)

		val position: ArrowBubblePosition = ArrowBubblePositioner.position(content, describable, view, preferredBelow = true)

		assertTrue(position.belowLocation)
		assertFalse(position.rightOfLocation)
		assertTrue(position.location.x > 550.0 + MIN_VIEW_DISTANCE)
	}

	private fun createView(dimension: Dimension2D, zoom: Double = 1.0): View<*> {
		val view = mock<View<*>>(MockMode.autofill)
		val canvas = CanvasMockBuilder().withDevicePixelRatio(1.0).withView(view).build()
		val slotX = Capture.slot<Double>()
		val slotY = Capture.slot<Double>()
		val slotPoint = Capture.slot<Point2D>()
		every { view.canvas } returns canvas
		every { view.width } returns dimension.width.toInt()
		every { view.height } returns dimension.height.toInt()
		every { view.modelToViewX(capture(slotX)) } calls { slotX.get() / zoom }
		every { view.modelToViewY(capture(slotY)) } calls { slotY.get() / zoom }
		every { view.modelToView(capture(slotPoint)) } calls { slotPoint.get().multiply(zoom) }
		every { view.modelToDeviceX(capture(slotX)) } calls { slotX.get() / canvas.devicePixelRatio }
		every { view.modelToDeviceY(capture(slotY)) } calls { slotY.get() / canvas.devicePixelRatio }
		every { view.modelToDevice(capture(slotPoint)) } calls { slotPoint.get().multiply(1 / canvas.devicePixelRatio)}
		return view
	}

	private fun createContent(dimension: Dimension2D): RectangularDrawable {
		val content = mock<RectangularDrawable>()
		every { content.width } returns dimension.width
		every { content.height } returns dimension.height
		return content
	}
}