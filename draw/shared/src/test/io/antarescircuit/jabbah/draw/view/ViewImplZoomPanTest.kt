package io.antarescircuit.jabbah.draw.view

import io.antarescircuit.jabbah.base.System
import io.antarescircuit.jabbah.base.geom.Dimension2D
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.draw.*
import io.antarescircuit.jabbah.draw.drawable.DrawableMockBuilder
import io.antarescircuit.jabbah.graphics.Graphics2DMockBuilder
import io.antarescircuit.jabbah.draw.module.DrawModule
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ViewImplZoomPanTest {

	private val graphics2D = Graphics2DMockBuilder()

	private val context = DrawModule.drawContextFactory(graphics2D.build(), null, null)

	private val view = ViewImpl(
		affineTransformFactory = { System.createAffineTransform() },
		viewPainterFactory = { SimpleViewPainter(it) },
		applicationContextHolder = null)

	@BeforeTest
	fun setup() {
		DrawTestRule.configure()
	}

	@Test
	fun shouldApplyNoZoomPan() {
		initializeAndDrawView(ZoomStrategy.NONE)

		assertEquals(1.0, view.zoomFactor)
		assertEquals(Point2D(0, 0), view.zoomPan.panOrigin)
		assertEquals(20.0, view.contentBounds.main.width)
		assertEquals(10.0, view.contentBounds.main.height)
		assertEquals(Rectangle2D(0, 0, 20, 10), graphics2D.drawnRectangle)
	}

	@Test
	fun shouldPanBy() {
		initializeAndDrawView(ZoomStrategy.NONE)

		view.navigator.panBy(3, 4)
		view.draw(context)

		assertEquals(Rectangle2D(3, 4, 20, 10), graphics2D.drawnRectangle)
	}

	@Test
	fun shouldZoomAtZero() {
		initializeAndDrawView(ZoomStrategy.NONE)

		view.navigator.setZoomFactor(2.0, Point2D.ZERO)
		view.draw(context)

		assertEquals(Rectangle2D(0, 0, 40, 20), graphics2D.drawnRectangle)
	}

	@Test
	fun shouldPanZoomed() {
		initializeAndDrawView(ZoomStrategy.NONE)

		view.navigator.setZoomFactor(2.0, Point2D.ZERO)
		view.navigator.panBy(30, 40) // View space
		view.draw(context)

		assertEquals(Point2D(-15, -20), view.zoomPan.panOrigin)
		assertEquals(Rectangle2D(30, 40, 40, 20), graphics2D.drawnRectangle)
	}

	@Test
	fun shouldCenterDrawableByDefault() {
		initializeAndDrawView()

		assertEquals(1.0, view.zoomFactor)
		assertEquals(Point2D(-40.0, -45.0), view.zoomPan.panOrigin)
		assertEquals(20.0, view.contentBounds.main.width)
		assertEquals(10.0, view.contentBounds.main.height)
		assertEquals(Rectangle2D(40, 45, 20, 10), graphics2D.drawnRectangle)
	}

	@Test
	fun shouldZoomAtLocation() {
		initializeAndDrawView(ZoomStrategy.NONE)

		// Zoom at lower-right corner of rectangle (view space = model space)
		view.navigator.multiplyZoomFactor(2.0, Point2D(20, 10))
		view.draw(context)

		assertEquals(2.0, view.zoomFactor)
		assertEquals(Rectangle2D(-20, -10, 40, 20), graphics2D.drawnRectangle)
	}

	@Test
	fun shouldZoomAtCenter() {
		initializeAndDrawView(ZoomStrategy.NONE)

		// Zoom at lower-right corner of rectangle (view space = model space)
		view.navigator.multiplyZoomFactor(2.0, null)
		view.draw(context)

		assertEquals(2.0, view.zoomFactor)
		assertEquals(Rectangle2D(-50, -50, 40, 20), graphics2D.drawnRectangle)
	}

	private fun initializeAndDrawView(defaultZoomStrategy: ZoomStrategy? = null) {
		defaultZoomStrategy?.let { view.zoomStrategy = it }
		defaultZoomStrategy?.let { view.defaultZoomStrategy = it }
		view.addDrawable(
			DrawableMockBuilder()
				.withBoundingBox(Rectangle2D(0, 0, 20, 10))
				.build())

		CanvasMockBuilder()
			.withDimension(Dimension2D(100, 100))
			.withView(view)

		view.initialize()
		view.draw(context)
	}
}