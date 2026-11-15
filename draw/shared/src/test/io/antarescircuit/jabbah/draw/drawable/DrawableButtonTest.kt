package io.antarescircuit.jabbah.draw.drawable

import io.antarescircuit.jabbah.base.event.*
import io.antarescircuit.jabbah.base.geom.Dimension2D
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.draw.*
import io.antarescircuit.jabbah.draw.style.Stylable
import io.antarescircuit.jabbah.draw.style.Style
import io.antarescircuit.jabbah.draw.graphics.Stroke
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import kotlin.test.*

class DrawableButtonTest {

	private var actions = 0
	private val view: View<*> = mock(MockMode.autofill)
	private val renderer: DrawableButtonRenderer = mock(MockMode.autofill)
	private val button: DrawableButton<InputEventContext>

	init {
		every { renderer.dimension } returns Dimension2D(100, 50)
		val stylable = mock<Stylable>(MockMode.autofill)
		val style = mock<Style>(MockMode.autofill)
		every { stylable.style } returns style
		every { style.stroke } returns Stroke()
		button = DrawableButton(
			location = Point2D.ZERO,
			stylable = stylable,
			action = { actions++ },
			renderer = renderer,
		)
	}

	private fun context(type: MouseEventType, x: Int, button: Button = Button.BUTTON1) = InputEventContext(
		view = view,
		mouseEvent = MouseEventImpl(type = type, x = x, y = 10, button = button),
		x = x.toDouble(),
		y = 10.0,
	)

	@Test
	fun shouldActivateOnReleaseInsideDespiteLargeMovement() {
		val press = context(MouseEventType.PRESSED, 5)
		val handler = button.getInputEventHandler(press)
		assertSame(handler, handler.mousePressed(press))
		assertEquals(0, actions)
		assertSame(handler, handler.mouseDragged(context(MouseEventType.DRAGGED, 200)))
		val release = context(MouseEventType.RELEASED, 90)
		handler.mouseReleased(release)
		handler.mouseClicked(context(MouseEventType.CLICKED, 90))
		assertEquals(1, actions)
		assertTrue(release.mouseEvent!!.isEventConsumed())
	}

	@Test
	fun shouldCancelReleaseOutsideAndRequireNewPress() {
		val press = context(MouseEventType.PRESSED, 5)
		val handler = button.getInputEventHandler(press)
		handler.mousePressed(press)
		handler.mouseReleased(context(MouseEventType.RELEASED, 110))
		handler.mouseReleased(context(MouseEventType.RELEASED, 5))
		assertEquals(0, actions)
	}

	@Test
	fun shouldNotActivateWhenDisabledBeforeRelease() {
		val press = context(MouseEventType.PRESSED, 5)
		val handler = button.getInputEventHandler(press)
		handler.mousePressed(press)
		button.enabled = false
		handler.mouseReleased(context(MouseEventType.RELEASED, 5))
		assertEquals(0, actions)
	}

	@Test
	fun shouldIgnoreOtherButtons() {
		val press = context(MouseEventType.PRESSED, 5, Button.BUTTON3)
		val handler = button.getInputEventHandler(press)
		assertNull(handler.mousePressed(press))
		handler.mouseReleased(context(MouseEventType.RELEASED, 5, Button.BUTTON3))
		assertEquals(0, actions)
	}
}
