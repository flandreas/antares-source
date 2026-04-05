package io.antarescircuit.jabbah.draw.view

import io.antarescircuit.jabbah.base.System
import io.antarescircuit.jabbah.base.geom.Dimension2D
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.draw.*
import io.antarescircuit.jabbah.draw.container.DrawableContainerImpl
import io.antarescircuit.jabbah.draw.drawable.DrawableMockBuilder
import io.antarescircuit.jabbah.graphics.Graphics2DMockBuilder
import io.antarescircuit.jabbah.draw.module.DrawModule
import dev.mokkery.verify
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

/** Unit tests for [ViewImpl].*/
class ViewImplTest {

	private val graphics2D = Graphics2DMockBuilder()

	private val context = DrawModule.drawContextFactory(graphics2D.build(), null, null)

	private val view = ViewImpl<InputEventContext>(
		affineTransformFactory = { System.createAffineTransform() },
		viewPainterFactory = { SimpleViewPainter(it) },
		applicationContextHolder = null
	)

	private lateinit var canvas: CanvasMockBuilder

	private val container = DrawableContainerImpl<Drawable>()

	@BeforeTest
	fun before() {
		DrawTestRule.configure()
		canvas = CanvasMockBuilder()
			.withDimension(Dimension2D(1000, 1000))
			.withView(view)
		view.addDrawable(container)
	}

	@Test
	fun shouldResetNavigation() {
		view.navigator.reset()
		assertEquals(Point2D(300, 500), view.modelToView(Point2D(300, 500)))
		assertEquals(Point2D(123, 456), view.viewToModel(Point2D(123, 456)))
	}

	@Test
	fun shouldRepaintCanvasWhenValidatingDrawable() {
		container.invalidate()
		container.validate()
		verify { canvas.build().repaint() }
	}

	@Test
	fun shouldInverseViewToModel() {
		// Arbitrary values
		view.navigator.setPanOrigin(Point2D(13, 43))
		view.navigator.setZoomFactor(2.0)
		view.navigator.panBy(20, 50)

		val viewLoc = Point2D(84, 123)
		val modelLoc = view.viewToModel(viewLoc)

		assertEquals(viewLoc, view.modelToView(modelLoc))
	}

	@Test
	fun shouldInverseModelToView() {
		// Arbitrary values
		view.navigator.setPanOrigin(Point2D(13, 43))
		view.navigator.setZoomFactor(2.0)
		view.navigator.panBy(20, 50)

		val modelLoc = Point2D(84, 123)
		val viewLoc = view.modelToView(modelLoc)

		assertEquals(modelLoc, view.viewToModel(viewLoc))
	}

	@Test
	@Ignore
	fun shouldUseHigherDevicePixelRatio() {
		canvas.withDevicePixelRatio(2.0)

		view.addDrawable(DrawableMockBuilder()
			.withBoundingBox(Rectangle2D(0, 0, 10, 10))
			.build())

		view.initialize()
		view.draw(context)

		assertEquals(Rectangle2D(990, 990, 20, 20), graphics2D.drawnRectangle)
	}
}