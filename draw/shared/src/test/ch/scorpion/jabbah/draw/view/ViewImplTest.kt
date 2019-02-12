package ch.scorpion.jabbah.draw.view

import ch.scorpion.jabbah.base.geom.AffineTransformImpl
import ch.scorpion.jabbah.draw.Canvas
import ch.scorpion.jabbah.draw.DrawTestRule
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.InputEventContext
import ch.scorpion.jabbah.draw.container.DrawableContainerImpl
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.BeforeTest
import kotlin.test.Test

/** Unit tests for [ViewImpl].*/
class ViewImplTest {

	private val canvas: Canvas = mockk(relaxed = true)

	@BeforeTest
	fun before() {
		DrawTestRule.configure()
	}

	@Test
	fun shouldRepaintCanvasWhenValidatingDrawable() {
		//DrawTestRule.configure()

		val view: ViewImpl<InputEventContext> = ViewImpl(
			canvas = canvas,
			transformFactory = { AffineTransformImpl() },
			viewPainterFactory = { SimpleViewPainter(it) })

		val container = DrawableContainerImpl<Drawable>()
		view.addDrawable(container)
		container.invalidate()

		container.validate()

		verify { canvas.repaint() }
	}

}