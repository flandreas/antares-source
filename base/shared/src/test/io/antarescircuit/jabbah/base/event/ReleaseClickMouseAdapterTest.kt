package io.antarescircuit.jabbah.base.event

import kotlin.test.*

class ReleaseClickMouseAdapterTest {

	private class Recorder : ReleaseClickMouseAdapter(15.0) {
		val events = mutableListOf<MouseEvent>()
		var consumeRelease = false
		var resetOnRelease = false
		var dragImmediately = false
		override fun onMousePressed(e: MouseEvent) {
			events.add(e)
			if (dragImmediately) startDragging()
		}
		override fun onMouseDragged(e: MouseEvent) { events.add(e) }
		override fun onMouseReleased(e: MouseEvent) {
			events.add(e)
			if (consumeRelease) e.consumeEvent()
			if (resetOnRelease) resetMouseGesture()
		}
		override fun onMouseClicked(e: MouseEvent) { events.add(e) }
	}

	private val adapter = Recorder()

	private fun event(type: MouseEventType, x: Int = 0, y: Int = 0, count: Int = 1, button: Button = Button.BUTTON1) =
		MouseEventImpl(type = type, x = x, y = y, clickCount = count, button = button, modifiers = Modifier.Alt.mask)

	@Test
	fun shouldDeliverSmallMovementImmediatelyWhenDragIsExplicitlyStarted() {
		adapter.dragImmediately = true
		adapter.mousePressed(event(MouseEventType.PRESSED))
		adapter.mouseDragged(event(MouseEventType.DRAGGED, 1))
		assertEquals(MouseEventType.DRAGGED, adapter.events.last().type)
		adapter.mouseReleased(event(MouseEventType.RELEASED, 1))
		assertEquals(listOf(MouseEventType.PRESSED, MouseEventType.DRAGGED, MouseEventType.RELEASED), adapter.events.map { it.type })
	}

	@Test
	fun shouldNotGenerateClickOrArtificialMovementForStationaryExplicitDrag() {
		adapter.dragImmediately = true
		adapter.mousePressed(event(MouseEventType.PRESSED))
		adapter.mouseReleased(event(MouseEventType.RELEASED))
		assertEquals(listOf(MouseEventType.PRESSED, MouseEventType.RELEASED), adapter.events.map { it.type })
	}

	@Test
	fun shouldRestoreThresholdForNextGestureAfterExplicitDrag() {
		adapter.dragImmediately = true
		adapter.mousePressed(event(MouseEventType.PRESSED))
		adapter.mouseReleased(event(MouseEventType.RELEASED))
		adapter.dragImmediately = false
		adapter.events.clear()
		adapter.mousePressed(event(MouseEventType.PRESSED))
		adapter.mouseDragged(event(MouseEventType.DRAGGED, 1))
		adapter.mouseReleased(event(MouseEventType.RELEASED, 1))
		assertEquals(listOf(MouseEventType.PRESSED, MouseEventType.RELEASED, MouseEventType.CLICKED), adapter.events.map { it.type })
	}

	@Test
	fun shouldClickAtReleaseLocationDespiteSmallMovement() {
		adapter.mousePressed(event(MouseEventType.PRESSED))
		adapter.mouseDragged(event(MouseEventType.DRAGGED, 9, 9))
		val release = event(MouseEventType.RELEASED, 9, 9)
		adapter.mouseReleased(release)

		assertEquals(listOf(MouseEventType.PRESSED, MouseEventType.RELEASED, MouseEventType.CLICKED), adapter.events.map { it.type })
		val click = adapter.events.last()
		assertEquals(release.location, click.location)
		assertEquals(release.modifiers, click.modifiers)
		click.consumeEvent()
		assertTrue(release.isEventConsumed())
	}

	@Test
	fun shouldIgnoreNativeClickAfterLogicalClick() {
		adapter.mousePressed(event(MouseEventType.PRESSED))
		adapter.mouseReleased(event(MouseEventType.RELEASED))
		adapter.mouseClicked(event(MouseEventType.CLICKED))
		assertEquals(1, adapter.events.count { it.type == MouseEventType.CLICKED })
	}

	@Test
	fun shouldUseEuclideanDistanceAndTreatThresholdAsDrag() {
		adapter.mousePressed(event(MouseEventType.PRESSED))
		adapter.mouseDragged(event(MouseEventType.DRAGGED, 9, 12))
		adapter.mouseReleased(event(MouseEventType.RELEASED, 9, 12))
		assertEquals(listOf(MouseEventType.PRESSED, MouseEventType.DRAGGED, MouseEventType.RELEASED), adapter.events.map { it.type })
	}

	@Test
	fun shouldNotClickWhenDragReturnsToOrigin() {
		adapter.mousePressed(event(MouseEventType.PRESSED))
		adapter.mouseDragged(event(MouseEventType.DRAGGED, 20))
		adapter.mouseDragged(event(MouseEventType.DRAGGED))
		adapter.mouseReleased(event(MouseEventType.RELEASED))
		assertEquals(0, adapter.events.count { it.type == MouseEventType.CLICKED })
	}

	@Test
	fun shouldCheckDistanceEvenWithoutMotionEvents() {
		adapter.mousePressed(event(MouseEventType.PRESSED))
		adapter.mouseReleased(event(MouseEventType.RELEASED, 20))
		assertEquals(0, adapter.events.count { it.type == MouseEventType.CLICKED })
		assertEquals(listOf(MouseEventType.PRESSED, MouseEventType.DRAGGED, MouseEventType.RELEASED), adapter.events.map { it.type })
	}

	@Test
	fun shouldRequireMatchingPressAndRelease() {
		adapter.mouseReleased(event(MouseEventType.RELEASED))
		adapter.mousePressed(event(MouseEventType.PRESSED))
		adapter.mouseReleased(event(MouseEventType.RELEASED, button = Button.BUTTON3))
		assertEquals(0, adapter.events.count { it.type == MouseEventType.CLICKED })
	}

	@Test
	fun shouldPreserveDoubleClickCountFromPress() {
		adapter.mousePressed(event(MouseEventType.PRESSED, count = 2))
		adapter.mouseReleased(event(MouseEventType.RELEASED, 1, count = 0))
		assertEquals(2, adapter.events.last().clickCount)
		assertEquals(MouseEventType.CLICKED, adapter.events.last().type)
	}

	@Test
	fun shouldNotClickAfterConsumedRelease() {
		adapter.consumeRelease = true
		adapter.mousePressed(event(MouseEventType.PRESSED))
		adapter.mouseReleased(event(MouseEventType.RELEASED))
		assertEquals(0, adapter.events.count { it.type == MouseEventType.CLICKED })
	}

	@Test
	fun shouldCancelClickWhenToolChangesDuringRelease() {
		adapter.resetOnRelease = true
		adapter.mousePressed(event(MouseEventType.PRESSED))
		adapter.mouseReleased(event(MouseEventType.RELEASED))
		assertEquals(0, adapter.events.count { it.type == MouseEventType.CLICKED })
	}

	@Test
	fun shouldCancelPendingPressWhenDeactivated() {
		adapter.mousePressed(event(MouseEventType.PRESSED))
		adapter.resetMouseGesture()
		adapter.mouseReleased(event(MouseEventType.RELEASED))
		assertEquals(0, adapter.events.count { it.type == MouseEventType.CLICKED })
	}
}
