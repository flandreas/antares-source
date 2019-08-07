package ch.scorpion.jabbah.draw

import ch.scorpion.jabbah.base.event.*
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.state.stateMachine
import ch.scorpion.jabbah.draw.graphics.Cursor
import io.mockk.mockk
import org.junit.Assert
import kotlin.test.Test
import kotlin.test.BeforeTest

class StateMachineInputEventHandlerTest {

	@BeforeTest
	fun before() {
		DrawTestRule.configure()
	}

	private val rectangle = TestRectangle(0, 0, 100, 100)
	private val view = mockk<View<*>>(relaxed = true)
	private val test = TestHandler()

	private inner class TestHandler {

		private var origRectangleLocation = Point2D()
		private var startDragLocation = Point2D()

		val handler = StateMachineInputEventHandler(
			stateMachine(strict = false) {

				state("sense") {
					onEntry { view.setCursor(Cursor.DEFAULT) }
					transitTo("inside") {
						given { it.mouseEvent?.type == MouseEventType.MOVED && rectangle.contains(it.location) }
					}
				}

				state("inside") {
					onEntry { view.setCursor(Cursor.HAND) }
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
						startDragLocation = it!!.location
					}
					transitTo("sense") {
						given { it.mouseEvent?.type == MouseEventType.RELEASED }
					}
					transitTo("drag") {
						given { it.mouseEvent?.type == MouseEventType.DRAGGED }
						onTransit { rectangle.location = origRectangleLocation.add(it!!.location.subtract(startDragLocation)) }
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
		test.handler.sm.start()
	}

	@Test
	fun testDragScenario() {
		test.handler.mouseMoved(context(MouseEventType.MOVED, 500, 500))
		Assert.assertEquals("sense", test.handler.sm.currentState.name)

		test.handler.mouseMoved(context(MouseEventType.MOVED, 50, 50))
		Assert.assertEquals("inside", test.handler.sm.currentState.name)

		test.handler.mouseMoved(context(MouseEventType.PRESSED, 50, 50))
		Assert.assertEquals("drag", test.handler.sm.currentState.name)

		test.handler.mouseMoved(context(MouseEventType.DRAGGED, 60, 50))
		Assert.assertEquals("drag", test.handler.sm.currentState.name)
		Assert.assertEquals(Point2D(10, 0), rectangle.location)

		test.handler.mouseMoved(context(MouseEventType.DRAGGED, 70, 50))
		Assert.assertEquals("drag", test.handler.sm.currentState.name)
		Assert.assertEquals(Point2D(20, 0), rectangle.location)

		test.handler.mouseMoved(context(MouseEventType.RELEASED, 60, 50))
		Assert.assertEquals("sense", test.handler.sm.currentState.name)
	}

	@Test
	fun testEscapeDragScenario() {

		test.handler.mouseMoved(context(MouseEventType.MOVED, 50, 50))
		Assert.assertEquals("inside", test.handler.sm.currentState.name)

		test.handler.mouseMoved(context(MouseEventType.PRESSED, 50, 50))
		Assert.assertEquals("drag", test.handler.sm.currentState.name)

		test.handler.mouseMoved(context(MouseEventType.DRAGGED, 60, 50))
		Assert.assertEquals("drag", test.handler.sm.currentState.name)
		Assert.assertEquals(Point2D(10, 0), rectangle.location)

		test.handler.keyPressed(context(KeyEventType.PRESSED, KeyEvent.VK_ESCAPE))
		Assert.assertEquals("sense", test.handler.sm.currentState.name)
		Assert.assertEquals(Point2D(0, 0), rectangle.location)

		test.handler.mouseMoved(context(MouseEventType.RELEASED, 60, 50))
		Assert.assertEquals("sense", test.handler.sm.currentState.name)
	}

	private fun context(type: MouseEventType, x: Int, y: Int): InputEventContext {
		return InputEventContext(view, mouseEvent = MouseEventImpl(type, x, y), x = x.toDouble(), y = y.toDouble())
	}

	private fun context(type: KeyEventType, keyCode: Int): InputEventContext {
		return InputEventContext(view, keyEvent = KeyEventImpl(type, key = keyCode, keyChar = ' '))
	}
}