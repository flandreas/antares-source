package ch.scorpion.jabbah.draw

import ch.scorpion.jabbah.base.event.*
import ch.scorpion.jabbah.base.event.MouseEventType.*
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.state.State
import ch.scorpion.jabbah.base.state.StateMachine
import ch.scorpion.jabbah.base.state.Transition
import ch.scorpion.jabbah.draw.graphics.Cursor
import io.mockk.mockk
import org.junit.Assert.*
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

		val sense: State<InputEventContext> = State<InputEventContext>(
			"sensing",
			entryAction = { view.setCursor(Cursor.DEFAULT) }
		)

		val inside: State<InputEventContext> = State<InputEventContext>(
			"inside",
			entryAction = { view.setCursor(Cursor.HAND) }
		)

		val drag: State<InputEventContext> = State<InputEventContext>(
			"drag",
			entryAction = {
				origRectangleLocation = rectangle.location
				startDragLocation = it!!.location
			}
		)

		val handler= StateMachineInputEventHandler(StateMachine(sense, inside, drag, strict = false))

		init {
			sense.add(Transition(inside, { it.mouseEvent?.type == MOVED && rectangle.contains(it.location) }))

			inside.add(Transition(sense, { it.mouseEvent?.type == MOVED && !rectangle.contains(it.location) }))
			inside.add(Transition(drag, { it.mouseEvent?.type == PRESSED }))

			drag.add(Transition(sense, { it.mouseEvent?.type == RELEASED }))
			drag.add(Transition(
				drag,
				condition = { it.mouseEvent?.type == DRAGGED },
				action = {
					rectangle.location = origRectangleLocation.add(it!!.location.subtract(startDragLocation))
				})
			)
			drag.add(Transition(
				sense,
				condition = { it.keyEvent?.type == KeyEventType.PRESSED },
				action = { rectangle.location = origRectangleLocation }
			))
		}
	}

	@Test
	fun testDragScenario() {
		test.handler.mouseMoved(context(MOVED, 500, 500))
		assertEquals(test.sense, test.handler.sm.currentState)

		test.handler.mouseMoved(context(MOVED, 50, 50))
		assertEquals(test.inside, test.handler.sm.currentState)

		test.handler.mouseMoved(context(PRESSED, 50, 50))
		assertEquals(test.drag, test.handler.sm.currentState)

		test.handler.mouseMoved(context(DRAGGED, 60, 50))
		assertEquals(test.drag, test.handler.sm.currentState)
		assertEquals(Point2D(10, 0), rectangle.location)

		test.handler.mouseMoved(context(DRAGGED, 70, 50))
		assertEquals(test.drag, test.handler.sm.currentState)
		assertEquals(Point2D(20, 0), rectangle.location)

		test.handler.mouseMoved(context(RELEASED, 60, 50))
		assertEquals(test.sense, test.handler.sm.currentState)
	}

	@Test
	fun testEscapeDragScenario() {

		test.handler.mouseMoved(context(MOVED, 50, 50))
		assertEquals(test.inside, test.handler.sm.currentState)

		test.handler.mouseMoved(context(PRESSED, 50, 50))
		assertEquals(test.drag, test.handler.sm.currentState)

		test.handler.mouseMoved(context(DRAGGED, 60, 50))
		assertEquals(test.drag, test.handler.sm.currentState)
		assertEquals(Point2D(10, 0), rectangle.location)

		test.handler.keyPressed(context(KeyEventType.PRESSED, KeyEvent.VK_ESCAPE))
		assertEquals(test.sense, test.handler.sm.currentState)
		assertEquals(Point2D(0, 0), rectangle.location)

		test.handler.mouseMoved(context(RELEASED, 60, 50))
		assertEquals(test.sense, test.handler.sm.currentState)
	}

	private fun context(type: MouseEventType, x: Int, y: Int): InputEventContext {
		return InputEventContext(view, mouseEvent = MouseEventImpl(type, x, y), x = x.toDouble(), y = y.toDouble())
	}

	private fun context(type: KeyEventType, keyCode: Int): InputEventContext {
		return InputEventContext(view, keyEvent = KeyEventImpl(type, key = keyCode, keyChar = ' '))
	}
}
