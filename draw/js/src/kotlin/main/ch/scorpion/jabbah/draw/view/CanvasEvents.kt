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

	private fun convertEventType(): MouseEventType {
		return when (event.type) {
			"click" -> MouseEventType.CLICKED
			"mousedown" -> MouseEventType.PRESSED
			"mouseup" -> MouseEventType.RELEASED
			"mouseenter" -> MouseEventType.ENTERED
			"mouseleave" -> MouseEventType.EXITED
			"mousemove" -> if (pressed) MouseEventType.DRAGGED else MouseEventType.MOVED
			"wheel" -> MouseEventType.WHEEL_ROTATED
			else -> MouseEventType.MOVED // TODO what to do else?
		}
	}

	private fun convertButton(): Button {
		return when (event.button.toInt()) {
			0 -> Button.BUTTON1
			1 -> Button.BUTTON2
			2 -> Button.BUTTON3
			else -> Button.NONE
		}
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

internal class KeyEventJs(
	private val canvas: HTMLCanvasElement,
	override val event: org.w3c.dom.events.KeyboardEvent
) : KeyEvent {

	override val type: KeyEventType get() = convertEventType()

	override val key: Int get() = event.keyCode

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