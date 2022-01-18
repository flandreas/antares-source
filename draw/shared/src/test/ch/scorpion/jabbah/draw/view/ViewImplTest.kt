package ch.scorpion.jabbah.draw.view

import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.draw.*
import ch.scorpion.jabbah.draw.container.DrawableContainerImpl
import ch.scorpion.jabbah.draw.drawable.DrawableMockBuilder
import ch.scorpion.jabbah.draw.graphics.Graphics2DMockBuilder
import ch.scorpion.jabbah.draw.module.DrawModule
import io.mockk.verify
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

/** Unit tests for [ViewImpl].*/
class ViewImplTest {

	private val graphics2D = Graphics2DMockBuilder()

	private val context = DrawModule.drawContextFactory(graphics2D.build(), null)

	private val view = ViewImpl<InputEventContext>(
		affineTransformFactory = { System.createAffineTransform() },
		viewPainterFactory = { SimpleViewPainter(it) },
		applicationContextHolder = null
	)

	private val canvas = CanvasMockBuilder()
		.withView(view)
		.withDimension(Dimension2D(1000, 1000))

	private val container = DrawableContainerImpl<Drawable>()

	@BeforeTest
	fun before() {
		DrawTestRule.configure()
		view.addDrawable(container)
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
		canvas.withDevicePixelRatio(2)

		view.addDrawable(DrawableMockBuilder()
			.withBoundingBox(Rectangle2D(0, 0, 10, 10))
			.build())

		view.initialize()
		view.draw(context)

		assertEquals(Rectangle2D(990, 990, 20, 20), graphics2D.drawnRectangle)
	}
}