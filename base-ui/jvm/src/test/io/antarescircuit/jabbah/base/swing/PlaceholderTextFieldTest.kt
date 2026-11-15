package io.antarescircuit.jabbah.base.swing

import java.awt.event.MouseEvent
import javax.swing.SwingUtilities
import kotlin.test.*

class PlaceholderTextFieldTest {

	@Test
	fun shouldClearOnReleaseInsideIconWithMovement() = SwingUtilities.invokeAndWait {
		val field = PlaceholderTextField(showClearButton = true).apply {
			setSize(200, 30)
			text = "query"
		}
		val listener = field.mouseListeners.filterIsInstance<ReleaseClickListenerSwing>().single()
		listener.mousePressed(MouseEvent(field, MouseEvent.MOUSE_PRESSED, 0, 0, 180, 15, 1, false, MouseEvent.BUTTON1))
		assertEquals("query", field.text)
		listener.mouseDragged(MouseEvent(field, MouseEvent.MOUSE_DRAGGED, 0, 0, 190, 15, 0, false, MouseEvent.NOBUTTON))
		listener.mouseReleased(MouseEvent(field, MouseEvent.MOUSE_RELEASED, 0, 0, 190, 15, 1, false, MouseEvent.BUTTON1))
		assertEquals("", field.text)
	}

	@Test
	fun shouldNotClearWhenPressStartedInText() = SwingUtilities.invokeAndWait {
		val field = PlaceholderTextField(showClearButton = true).apply {
			setSize(200, 30)
			text = "query"
		}
		val listener = field.mouseListeners.filterIsInstance<ReleaseClickListenerSwing>().single()
		listener.mousePressed(MouseEvent(field, MouseEvent.MOUSE_PRESSED, 0, 0, 20, 15, 1, false, MouseEvent.BUTTON1))
		listener.mouseReleased(MouseEvent(field, MouseEvent.MOUSE_RELEASED, 0, 0, 190, 15, 1, false, MouseEvent.BUTTON1))
		assertEquals("query", field.text)
	}
}
