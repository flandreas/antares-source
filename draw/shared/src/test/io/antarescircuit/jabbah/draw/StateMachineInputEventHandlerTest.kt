package io.antarescircuit.jabbah.draw

import io.antarescircuit.jabbah.base.event.*
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.state.UnhandledEventBehaviour.Unhandled
import io.antarescircuit.jabbah.base.state.stateMachine
import io.antarescircuit.jabbah.draw.graphics.Cursor
import dev.mokkery.MockMode
import dev.mokkery.mock
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class StateMachineInputEventHandlerTest {

	private val rectangle = TestRectangle(0, 0, 100, 100)
	private val view = mock<View<*>>(MockMode.autofill)
	private val test = TestHandler()

	private inner class TestHandler {

		private var origRectangleLocation = Point2D()
		private var startDragLocation = Point2D()

		val handler = StateMachineInputEventHandler(
			stateMachine(behaviour = Unhandled) {

				state("sense") {
					onEntry { view.setCursor(Cursor.DEFAULT) }
					transitTo("inside") {
						given { it.mouseEvent?.type == MouseEventType.MOVED && rectangle.contains(it.location) }
					}
				}

				state("inside") {
					onEntry { view.setCursor(Cursor.MOVE) }
					transitTo("sense") {
						given {  it.mouseEvent?.type == MouseEventType.MOVED && !rectangle.contains(it.location) }
					}
					transitTo("drag") {
						given { it.mouseEvent?.type == MouseEventType.PRESSED }
					}
				}

				state("drag") {
					onEntry {
						origRectangleLocation = rectangle.location
						startDragLocation = it.location
					}
					transitTo("sense") {
						given { it.mouseEvent?.type == MouseEventType.RELEASED }
					}
					transitTo("drag") {
						given { it.mouseEvent?.type == MouseEventType.DRAGGED }
						onTransit { rectangle.location = origRectangleLocation.add(it.location.subtract(startDragLocation)) }
					}
					transitTo("sense") {
						given { it.keyEvent?.type == KeyEventType.PRESSED }
						onTransit { rectangle.location = origRectangleLocation }
					}
				}
			}
		)
	}

	init {
		test.handler.sm.start(context(MouseEventType.MOVED, 0, 0))
	}

	@BeforeTest
	fun setup() {
		DrawTestRule.configure()
	}

	@Test
	fun testDragScenario() {
		test.handler.mouseMoved(context(MouseEventType.MOVED, 500, 500))
		assertEquals("sense", test.handler.sm.currentState.name)

		test.handler.mouseMoved(context(MouseEventType.MOVED, 50, 50))
		assertEquals("inside", test.handler.sm.currentState.name)

		test.handler.mouseMoved(context(MouseEventType.PRESSED, 50, 50))
		assertEquals("drag", test.handler.sm.currentState.name)

		test.handler.mouseMoved(context(MouseEventType.DRAGGED, 60, 50))
		assertEquals("drag", test.handler.sm.currentState.name)
		assertEquals(Point2D(10, 0), rectangle.location)

		test.handler.mouseMoved(context(MouseEventType.DRAGGED, 70, 50))
		assertEquals("drag", test.handler.sm.currentState.name)
		assertEquals(Point2D(20, 0), rectangle.location)

		test.handler.mouseMoved(context(MouseEventType.RELEASED, 60, 50))
		assertEquals("sense", test.handler.sm.currentState.name)
	}

	@Test
	fun testEscapeDragScenario() {

		test.handler.mouseMoved(context(MouseEventType.MOVED, 50, 50))
		assertEquals("inside", test.handler.sm.currentState.name)

		test.handler.mouseMoved(context(MouseEventType.PRESSED, 50, 50))
		assertEquals("drag", test.handler.sm.currentState.name)

		test.handler.mouseMoved(context(MouseEventType.DRAGGED, 60, 50))
		assertEquals("drag", test.handler.sm.currentState.name)
		assertEquals(Point2D(10, 0), rectangle.location)

		test.handler.keyPressed(context(KeyEventType.PRESSED, KeyEvent.VK_ESCAPE))
		assertEquals("sense", test.handler.sm.currentState.name)
		assertEquals(Point2D(0, 0), rectangle.location)

		test.handler.mouseMoved(context(MouseEventType.RELEASED, 60, 50))
		assertEquals("sense", test.handler.sm.currentState.name)
	}

	private fun context(type: MouseEventType, x: Int, y: Int): InputEventContext {
		return InputEventContext(view, mouseEvent = MouseEventImpl(type, x = x, y = y), x = x.toDouble(), y = y.toDouble())
	}

	private fun context(type: KeyEventType, keyCode: Int): InputEventContext {
		return InputEventContext(view, keyEvent = KeyEventImpl(type, key = keyCode, keyChar = ' '))
	}
}