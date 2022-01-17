package ch.scorpion.jabbah.draw.view

import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.draw.CanvasMockBuilder
import ch.scorpion.jabbah.draw.DrawTestRule
import ch.scorpion.jabbah.draw.InputEventContext
import ch.scorpion.jabbah.draw.drawable.DrawableMockBuilder
import ch.scorpion.jabbah.draw.graphics.Graphics2DMockBuilder
import ch.scorpion.jabbah.draw.module.DrawModule
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ViewImplZoomPanTest {

	private val graphics2D = Graphics2DMockBuilder()

	private val context = DrawModule.drawContextFactory(graphics2D.build(), null)

	private val view = ViewImpl<InputEventContext>(
		transformFactory = { System.createAffineTransform() },
		viewPainterFactory = { SimpleViewPainter(it) },
		applicationContextHolder = null)

	private val canvas = CanvasMockBuilder()
		.withDimension(Dimension2D(100, 100))
		.withView(view)

	@BeforeTest
	fun setup() {
		DrawTestRule.configure()
	}

	@Test
	fun shouldCenterDrawableByDefault() {
		view.addDrawable(
			DrawableMockBuilder()
			.withBoundingBox(Rectangle2D(0, 0, 20, 10))
			.build())

		view.initialize()
		view.draw(context)

		assertEquals(1.0, view.zoomFactor)
		assertEquals(20.0, view.contentBounds.main.width)
		assertEquals(10.0, view.contentBounds.main.height)
		assertEquals(Rectangle2D(40, 45, 20, 10), graphics2D.drawnRectangle)
	}
}