package ch.scorpion.jabbah.draw.view

import ch.scorpion.jabbah.base.event.*
import ch.scorpion.jabbah.base.geom.Point2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.EventListener


internal class MouseEventJs(
	override val location: Point2D,
	private val canvas: HTMLCanvasElement,
	override val event: org.w3c.dom.events.MouseEvent,
	private val pressed: Boolean = false
) : MouseEvent {

	override val type: MouseEventType = convertEventType()

	override val source: Any get() = canvas

	override val button: Button get() = convertButton()

	override val clickCount: Int get() = if (event.type == "dblclick") 2 else 1

	override val modifiers: Int get() = convertModifiers()

	override val wheelRotation: Int
		get() = if (event is org.w3c.dom.events.WheelEvent) event.deltaY.toInt() else 0

	override val x: Int get() = location.xInt

	override val y: Int get() = location.yInt

	override fun consume() {
		event.preventDefault()
	}

	override fun isConsumed(): Boolean = event.defaultPrevented

	override val isLeftButtonDown: Boolean get() = button == Button.BUTTON1

	override val isMiddleButtonDown: Boolean get() = button == Button.BUTTON2

	override val isRightButtonDown: Boolean get() = button == Button.BUTTON3

	override fun toString(): String = "MouseEvent $type"

	private fun convertEventType(): MouseEventType =
		when (event.type) {
			"click" -> MouseEventType.CLICKED
			"mousedown" -> MouseEventType.PRESSED
			"mouseup" -> MouseEventType.RELEASED
			"mouseenter" -> MouseEventType.ENTERED
			"mouseleave" -> MouseEventType.EXITED
			"mousemove" -> if (pressed) MouseEventType.DRAGGED else MouseEventType.MOVED
			"wheel" -> MouseEventType.WHEEL_ROTATED
			else -> MouseEventType.UNKNOWN
		}

	private fun convertButton(): Button =
		when (event.button.toInt()) {
			0 -> Button.BUTTON1
			1 -> Button.BUTTON2
			2 -> Button.BUTTON3
			else -> Button.NONE
		}

	private fun convertModifiers(): Int {
		var modifiers = 0
		if (event.shiftKey) {
			modifiers = modifiers or Modifier.Shift.mask
		}
		if (event.ctrlKey) {
			modifiers = modifiers or Modifier.Ctrl.mask
		}
		if (event.metaKey) {
			modifiers = modifiers or Modifier.Meta.mask
		}
		if (event.altKey) {
			modifiers = modifiers or Modifier.Alt.mask
		}

		return modifiers
	}
}

internal class MouseEventBridge(
	private val windowToCanvas: (org.w3c.dom.events.MouseEvent) -> Point2D,
	val listener: MouseListener,
	private val canvas: HTMLCanvasElement
) : EventListener {

	override fun handleEvent(event: Event) {
		val e = MouseEventJs(windowToCanvas.invoke(event as org.w3c.dom.events.MouseEvent), canvas, event)
		when (event.type) {
			"mousedown" -> listener.mousePressed(e)
			"mouseup" -> listener.mouseReleased(e)
			"click" -> listener.mouseClicked(e)
			"dblclick" -> listener.mouseClicked(e)
		}
	}
}

internal class MouseMotionEventBridge(
	private val windowToCanvas: (org.w3c.dom.events.MouseEvent) -> Point2D,
	val listener: MouseMotionListener,
	private val canvas: HTMLCanvasElement
) : EventListener {

	private var pressed: Boolean = false

	override fun handleEvent(event: Event) {
		val e = MouseEventJs(windowToCanvas.invoke(event as org.w3c.dom.events.MouseEvent), canvas, event, pressed)
		when (event.type) {
			"mousedown" -> pressed = true
			"mouseup" -> pressed = false
			"mousemove" -> {
				if (pressed) {
					listener.mouseDragged(e)
				} else {
					listener.mouseMoved(e)
				}
			}
		}
	}
}

internal class MouseWheelEventBridge(
	private val windowToCanvas: (org.w3c.dom.events.MouseEvent) -> Point2D,
	val listener: MouseWheelListener,
	private val canvas: HTMLCanvasElement
) : EventListener {

	override fun handleEvent(event: Event) {
		val w3cEvent = event as org.w3c.dom.events.WheelEvent
		val e = MouseEventJs(windowToCanvas.invoke(w3cEvent), canvas, w3cEvent)
		listener.mouseWheelRotated(e)
	}
}

private val keyboardEventKeyMap = mapOf<String, Int>(
	"arrowleft" to KeyEvent.VK_LEFT,
	"arrowright" to KeyEvent.VK_RIGHT,
	"arrowup" to KeyEvent.VK_UP,
	"arrowdown" to KeyEvent.VK_DOWN,
	"arrowdown" to KeyEvent.VK_DOWN,
	"escape" to KeyEvent.VK_ESCAPE,
	"alt" to KeyEvent.VK_ALT,
	"enter" to KeyEvent.VK_ENTER,
	"delete" to KeyEvent.VK_DELETE,
	" " to KeyEvent.VK_SPACE,
	"0" to KeyEvent.VK_0,
	"1" to KeyEvent.VK_1,
	"2" to KeyEvent.VK_2,
	"3" to KeyEvent.VK_3,
	"4" to KeyEvent.VK_4,
	"5" to KeyEvent.VK_5,
	"6" to KeyEvent.VK_6,
	"7" to KeyEvent.VK_7,
	"8" to KeyEvent.VK_8,
	"9" to KeyEvent.VK_9,
	"a" to KeyEvent.VK_A,
	"b" to KeyEvent.VK_B,
	"c" to KeyEvent.VK_C,
	"d" to KeyEvent.VK_D,
	"e" to KeyEvent.VK_E,
	"f" to KeyEvent.VK_F,
	"x" to KeyEvent.VK_X,
	"z" to KeyEvent.VK_Z,
)

internal class KeyEventJs(
	private val canvas: HTMLCanvasElement,
	override val event: org.w3c.dom.events.KeyboardEvent
) : KeyEvent {

	override val type: KeyEventType get() = convertEventType()

	override val key: Int get() = keyboardEventKeyMap[event.key.lowercase()] ?: 0

	override val keyChar: Char get() = event.charCode.toChar()

	override val source: Any get() = canvas

	override val modifiers: Int get() = convertModifiers()

	override fun consume() {
		event.preventDefault()
	}

	override fun isConsumed(): Boolean = event.defaultPrevented

	private fun convertModifiers(): Int {
		var modifiers = 0
		if (event.shiftKey) {
			modifiers = modifiers or Modifier.Shift.mask
		}
		if (event.ctrlKey) {
			modifiers = modifiers or Modifier.Ctrl.mask
		}
		if (event.metaKey) {
			modifiers = modifiers or Modifier.Meta.mask
		}
		if (event.altKey) {
			modifiers = modifiers or Modifier.Alt.mask
		}

		return modifiers
	}

	private fun convertEventType(): KeyEventType {
		return when(event.type) {
			"keydown" -> KeyEventType.PRESSED
			"keyup" -> KeyEventType.RELEASED
			else -> KeyEventType.TYPED
		}
	}
}

internal class KeyEventBridge(val listener: KeyListener, val canvas: HTMLCanvasElement) : EventListener {

	override fun handleEvent(event: Event) {
		val e = KeyEventJs(canvas, event as org.w3c.dom.events.KeyboardEvent)
		when (e.type) {
			KeyEventType.TYPED -> listener.keyTyped(e)
			KeyEventType.PRESSED -> listener.keyPressed(e)
			KeyEventType.RELEASED -> listener.keyReleased(e)
		}
	}
}