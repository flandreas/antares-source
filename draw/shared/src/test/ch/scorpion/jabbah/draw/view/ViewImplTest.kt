package ch.scorpion.jabbah.draw.view

import ch.scorpion.jabbah.base.geom.AffineTransformImpl
import ch.scorpion.jabbah.draw.Canvas
import ch.scorpion.jabbah.draw.DrawTestRule
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.InputEventContext
import ch.scorpion.jabbah.draw.container.DrawableContainerImpl
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.ClassRule
import org.junit.Test
import org.mockito.Mockito

/** Unit tests for [ViewImpl].*/
class ViewImplTest {

	companion object {
		@ClassRule
		@JvmField
		val drawTestRule = DrawTestRule()
	}

	private val canvas: Canvas = mock()

	private val view: ViewImpl<InputEventContext> = ViewImpl(
		canvas = canvas,
		transformFactory = { AffineTransformImpl() },
		viewPainterFactory = { SimpleViewPainter(it) })

	@Test
	fun shouldRepaintCanvasWhenValidatingDrawable() {
		val container = DrawableContainerImpl<Drawable>()
		view.addDrawable(container)
		Mockito.clearInvocations(canvas)
		container.invalidate()

		container.validate()

		verify(canvas).repaint()
	}

}