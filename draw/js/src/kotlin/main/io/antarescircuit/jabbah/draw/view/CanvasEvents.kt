package io.antarescircuit.jabbah.draw.view

import io.antarescircuit.jabbah.base.event.*
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.draw.View
import io.antarescircuit.jabbah.draw.ZoomStrategy
import kotlinx.browser.window
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

    override val clickCount: Int get() = event.detail.coerceAtLeast(1)

    override val modifiers: Int get() = convertModifiers()

    override val wheelRotation: Point2D
        get() = if (event is org.w3c.dom.events.WheelEvent) Point2D(-event.deltaX.toInt(), -event.deltaY.toInt()) else Point2D.ZERO

    override val x: Int get() = location.xInt

    override val y: Int get() = location.yInt

    override fun consumeEvent() {
        event.preventDefault()
    }

    override fun isEventConsumed(): Boolean = event.defaultPrevented

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
    "shift" to KeyEvent.VK_SHIFT,
    "meta" to KeyEvent.VK_META,
    "control" to KeyEvent.VK_CTRL,
    "altgraph" to KeyEvent.VK_ALT_GRAPH,
    "enter" to KeyEvent.VK_ENTER,
    "delete" to KeyEvent.VK_DELETE,
    "backspace" to KeyEvent.VK_DELETE,
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

    override val keyChar: Char get() = if (event.key.length == 1) event.key[0] else 0.toChar()

    override val source: Any get() = canvas

    override val modifiers: Int get() = convertModifiers()

    override fun consumeEvent() {
        event.preventDefault()
    }

    override fun isEventConsumed(): Boolean = event.defaultPrevented

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
            else -> {}
        }
    }
}
/**
 * Makes a canvas usable with touch screens by translating touch input into the mouse events
 * that the shared editor code understands; mouse input is not affected.
 *
 * - one-finger drag: mouse drag (move parts, draw wires, select an area)
 * - tap: click; double-tap: double click; long press: context menu (`contextmenu` event)
 * - two fingers: pan and pinch zoom of [view]
 *
 * Touch events are used instead of pointer events because preventing their default action
 * also suppresses the browser's emulated mouse events, including `click`.
 */
internal class TouchInput(
    private val canvas: HTMLCanvasElement,
    private val view: View<*>,
) {
    private enum class Mode { IDLE, PENDING, DRAGGING, GESTURE }

    private companion object {
        /** Movement in CSS pixels after which a touch is a drag instead of a tap. */
        const val DRAG_THRESHOLD = 8.0
        const val DOUBLE_TAP_TIME = 350.0
        const val DOUBLE_TAP_DISTANCE = 24.0
        const val LONG_PRESS_TIME = 550
    }

    private var mode = Mode.IDLE
    private var start = Point2D.ZERO
    private var last = Point2D.ZERO
    private var clickCount = 1
    private var lastTapTime = 0.0
    private var lastTap = Point2D.ZERO
    private var longPressTimer: Int? = null
    private var gestureCenter = Point2D.ZERO
    private var gestureDistance = 0.0

    init {
        canvas.style.setProperty("touch-action", "none")
        val options: dynamic = js("({ passive: false })")
        canvas.addEventListener("touchstart", { e -> onStart(e) }, options)
        canvas.addEventListener("touchmove", { e -> onMove(e) }, options)
        canvas.addEventListener("touchend", { e -> onEnd(e) }, options)
        canvas.addEventListener("touchcancel", { e -> onEnd(e) }, options)
    }

    private fun onStart(event: Event) {
        event.preventDefault()
        val touches = touchesOf(event, "touches")
        when {
            touches.size == 1 && mode == Mode.IDLE -> {
                val p = touches[0]
                val now = kotlin.js.Date.now()
                clickCount = if (now - lastTapTime < DOUBLE_TAP_TIME && p.distance(lastTap) < DOUBLE_TAP_DISTANCE) 2 else 1
                start = p
                last = p
                mode = Mode.PENDING
                canvas.focus()
                // Lets the editor update hover feedback (e.g. of pins) like a mouse would before pressing
                dispatch("mousemove", p, buttons = 0)
                longPressTimer = window.setTimeout(::onLongPress, LONG_PRESS_TIME)
            }
            touches.size >= 2 -> {
                cancelLongPress()
                if (mode == Mode.DRAGGING) {
                    dispatch("mouseup", last, buttons = 0)
                }
                mode = Mode.GESTURE
                gestureCenter = touches[0].center(touches[1])
                gestureDistance = touches[0].distance(touches[1])
            }
        }
    }

    private fun onMove(event: Event) {
        event.preventDefault()
        val touches = touchesOf(event, "touches")
        when (mode) {
            Mode.PENDING -> {
                val p = touches.firstOrNull() ?: return
                if (p.distance(start) > DRAG_THRESHOLD) {
                    cancelLongPress()
                    mode = Mode.DRAGGING
                    dispatch("mousedown", start, buttons = 1)
                    dispatch("mousemove", p, buttons = 1)
                    last = p
                }
            }
            Mode.DRAGGING -> {
                val p = touches.firstOrNull() ?: return
                dispatch("mousemove", p, buttons = 1)
                last = p
            }
            Mode.GESTURE -> if (touches.size >= 2) gesture(touches[0], touches[1])
            Mode.IDLE -> {}
        }
    }

    private fun onEnd(event: Event) {
        event.preventDefault()
        if (touchesOf(event, "touches").isNotEmpty()) return
        cancelLongPress()
        val p = touchesOf(event, "changedTouches").firstOrNull() ?: last
        when (mode) {
            Mode.PENDING -> if (event.type == "touchend") tap(start)
            Mode.DRAGGING -> {
                dispatch("mouseup", p, buttons = 0)
                // Browsers report a click after pressing and releasing the mouse on the same element, also after dragging
                dispatch("click", p, buttons = 0)
            }
            else -> {}
        }
        mode = Mode.IDLE
    }

    private fun tap(p: Point2D) {
        dispatch("mousedown", p, buttons = 1, detail = clickCount)
        dispatch("mouseup", p, buttons = 0, detail = clickCount)
        dispatch("click", p, buttons = 0, detail = clickCount)
        if (clickCount == 2) {
            dispatch("dblclick", p, buttons = 0, detail = 2)
            lastTapTime = 0.0
        } else {
            lastTapTime = kotlin.js.Date.now()
            lastTap = p
        }
    }

    private fun onLongPress() {
        longPressTimer = null
        if (mode != Mode.PENDING) return
        mode = Mode.IDLE
        dispatch("contextmenu", start, buttons = 0, button = 2)
    }

    private fun cancelLongPress() {
        longPressTimer?.let { window.clearTimeout(it) }
        longPressTimer = null
    }

    /** Zooms around and pans with the center between two fingers. */
    private fun gesture(a: Point2D, b: Point2D) {
        val center = a.center(b)
        val distance = a.distance(b)
        val ratio = window.devicePixelRatio
        view.zoomStrategy = ZoomStrategy.NONE
        if (gestureDistance > 0 && distance > 0) {
            val factor = distance / gestureDistance
            if (view.navigator.isZoomFactorInValidRange(view.zoomFactor * factor)) {
                view.navigator.multiplyZoomFactor(factor, Point2D(gestureCenter.x * ratio, gestureCenter.y * ratio))
            }
        }
        val dx = ((center.x - gestureCenter.x) * ratio).toInt()
        val dy = ((center.y - gestureCenter.y) * ratio).toInt()
        if (dx != 0 || dy != 0) view.navigator.panBy(dx, dy)
        gestureCenter = center
        gestureDistance = distance
    }

    /** Dispatches a synthetic mouse event at [p] (CSS pixels relative to the canvas). */
    private fun dispatch(type: String, p: Point2D, buttons: Int, detail: Int = 1, button: Int = 0) {
        val rect = canvas.getBoundingClientRect()
        val init: dynamic = js("({ bubbles: true, cancelable: true, view: window })")
        init.clientX = rect.left + p.x
        init.clientY = rect.top + p.y
        init.button = button
        init.buttons = buttons
        init.detail = detail
        canvas.dispatchEvent(org.w3c.dom.events.MouseEvent(type, init.unsafeCast<org.w3c.dom.events.MouseEventInit>()))
    }

    /** Returns the touch points of the touch list [name] of [event] in CSS pixels relative to the canvas. */
    private fun touchesOf(event: Event, name: String): List<Point2D> {
        val list = event.asDynamic()[name] ?: return emptyList()
        val rect = canvas.getBoundingClientRect()
        val length = list.length as Int
        return (0 until length).map { i ->
            val t = list[i]
            Point2D((t.clientX as Double) - rect.left, (t.clientY as Double) - rect.top)
        }
    }

    private fun Point2D.center(other: Point2D): Point2D = Point2D((x + other.x) / 2, (y + other.y) / 2)
}
