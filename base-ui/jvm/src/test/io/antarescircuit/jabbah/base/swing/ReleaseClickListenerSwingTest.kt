package io.antarescircuit.jabbah.base.swing

import java.awt.event.MouseEvent
import javax.swing.JPanel
import javax.swing.SwingUtilities
import kotlin.test.*

class ReleaseClickListenerSwingTest {

	private val component = JPanel().apply { setSize(100, 50) }
	private val clicks = mutableListOf<MouseEvent>()

	private fun event(id: Int, x: Int, y: Int = 10, count: Int = 1, button: Int = MouseEvent.BUTTON1) =
		MouseEvent(component, id, 0, 0, x, y, count, false, button)

	private fun listener(count: Int? = null, threshold: Double = 5.0) = object : ReleaseClickListenerSwing(count, threshold) {
		override fun targetAt(e: MouseEvent): Any? =
			if (component.contains(e.point)) e.x / 10 else null
		override fun clicked(e: MouseEvent) { clicks.add(e) }
	}

	@Test
	fun shouldActivateOnReleaseWithJitterAndIgnoreNativeClick() = SwingUtilities.invokeAndWait {
		val listener = listener()
		listener.installOn(component)
		listener.installOn(component)
		assertEquals(1, component.mouseListeners.count { it === listener })
		assertEquals(1, component.mouseMotionListeners.count { it === listener })
		component.dispatchEvent(event(MouseEvent.MOUSE_PRESSED, 11))
		assertTrue(clicks.isEmpty())
		component.dispatchEvent(event(MouseEvent.MOUSE_DRAGGED, 13))
		component.dispatchEvent(event(MouseEvent.MOUSE_RELEASED, 13))
		component.dispatchEvent(event(MouseEvent.MOUSE_CLICKED, 13))
		assertEquals(listOf(MouseEvent.MOUSE_RELEASED), clicks.map { it.id })
	}

	@Test
	fun shouldRejectDifferentRowsAndBlankArea() {
		val listener = listener()
		listener.mousePressed(event(MouseEvent.MOUSE_PRESSED, 19))
		listener.mouseReleased(event(MouseEvent.MOUSE_RELEASED, 20))
		listener.mousePressed(event(MouseEvent.MOUSE_PRESSED, 99))
		listener.mouseReleased(event(MouseEvent.MOUSE_RELEASED, 101))
		assertTrue(clicks.isEmpty())
	}

	@Test
	fun shouldRejectReleaseOutsideComponentEvenWhenRowLookupStillMatches() {
		val listener = object : ReleaseClickListenerSwing() {
			override fun targetAt(e: MouseEvent): Any = 0
			override fun clicked(e: MouseEvent) { clicks.add(e) }
		}
		listener.mousePressed(event(MouseEvent.MOUSE_PRESSED, 99))
		listener.mouseReleased(event(MouseEvent.MOUSE_RELEASED, 101))
		assertTrue(clicks.isEmpty())
	}

	@Test
	fun shouldRejectDragReturningToSameRow() {
		val listener = listener()
		listener.mousePressed(event(MouseEvent.MOUSE_PRESSED, 11))
		listener.mouseDragged(event(MouseEvent.MOUSE_DRAGGED, 31))
		listener.mouseReleased(event(MouseEvent.MOUSE_RELEASED, 11))
		assertTrue(clicks.isEmpty())
	}

	@Test
	fun shouldRecognizeDoubleClickFromSecondPressWithoutNativeClick() {
		val listener = listener(count = 2)
		listener.mousePressed(event(MouseEvent.MOUSE_PRESSED, 11))
		listener.mouseReleased(event(MouseEvent.MOUSE_RELEASED, 12))
		assertTrue(clicks.isEmpty())
		listener.mousePressed(event(MouseEvent.MOUSE_PRESSED, 11, count = 2))
		listener.mouseReleased(event(MouseEvent.MOUSE_RELEASED, 12, count = 0))
		assertEquals(1, clicks.size)
	}

	@Test
	fun shouldAllowMovementInsideWholeTarget() {
		val listener = object : ReleaseClickListenerSwing(dragThreshold = Double.POSITIVE_INFINITY) {
			override fun clicked(e: MouseEvent) { clicks.add(e) }
		}
		listener.mousePressed(event(MouseEvent.MOUSE_PRESSED, 1))
		listener.mouseDragged(event(MouseEvent.MOUSE_DRAGGED, 200))
		listener.mouseReleased(event(MouseEvent.MOUSE_RELEASED, 90))
		assertEquals(1, clicks.size)
	}

	@Test
	fun shouldIgnoreOtherButtonsAndUnpairedRelease() {
		val listener = listener()
		listener.mouseReleased(event(MouseEvent.MOUSE_RELEASED, 11))
		listener.mousePressed(event(MouseEvent.MOUSE_PRESSED, 11, button = MouseEvent.BUTTON3))
		listener.mouseReleased(event(MouseEvent.MOUSE_RELEASED, 11, button = MouseEvent.BUTTON3))
		assertTrue(clicks.isEmpty())
	}

	@Test
	fun shouldCancelPendingPressWhenUninstalled() {
		val listener = listener()
		listener.installOn(component)
		listener.mousePressed(event(MouseEvent.MOUSE_PRESSED, 11))
		listener.uninstallFrom(component)
		listener.installOn(component)
		listener.mouseReleased(event(MouseEvent.MOUSE_RELEASED, 11))
		assertTrue(clicks.isEmpty())
	}
}
