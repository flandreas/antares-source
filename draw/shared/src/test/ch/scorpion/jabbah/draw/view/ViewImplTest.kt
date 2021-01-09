package ch.scorpion.jabbah.draw.view

import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.Canvas
import ch.scorpion.jabbah.draw.DrawTestRule
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.InputEventContext
import ch.scorpion.jabbah.draw.container.DrawableContainerImpl
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** Unit tests for [ViewImpl].*/
class ViewImplTest {

	private val canvas: Canvas = mockk(relaxed = true)

	private lateinit var view: ViewImpl<InputEventContext>
	private val container = DrawableContainerImpl<Drawable>()

	@BeforeTest
	fun before() {
		DrawTestRule.configure()
		every { canvas.dimension } returns Dimension2D(1000, 1000)
		view = ViewImpl(
			canvas = canvas,
			transformFactory = { System.createAffineTransform() },
			viewPainterFactory = { SimpleViewPainter(it) },
			applicationContextHolder = null)
		view.addDrawable(container)
	}

	@Test
	fun shouldRepaintCanvasWhenValidatingDrawable() {
		container.invalidate()
		container.validate()
		verify { canvas.repaint() }
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

}