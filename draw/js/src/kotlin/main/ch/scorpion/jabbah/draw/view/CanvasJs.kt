package ch.scorpion.jabbah.draw.view

import ch.scorpion.jabbah.base.event.*
import ch.scorpion.jabbah.base.exception.UnsupportedOperationException
import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.draw.Canvas
import ch.scorpion.jabbah.draw.InputEventContext
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.Cursor
import ch.scorpion.jabbah.draw.graphics.Graphics2DJs
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.EventListener
import kotlinx.browser.document
import kotlinx.browser.window

/**
 * Maps a HTML canvas to the [Canvas] interface.
 */
class CanvasJs(
	id: String,
	override val view: View<out InputEventContext>,
	width: Int,
	height:Int,
	styleProvider: StyleProvider = DrawStyleModule.styleProvider
) : Canvas {

	companion object {
		private val LOG by logger(CanvasJs::class)
	}

	private val mouseListeners: MutableList<MouseEventBridge> by lazy { mutableListOf() }
	private val mouseMotionListeners: MutableList<MouseMotionEventBridge> by lazy { mutableListOf() }
	private val mouseWheelListeners: MutableList<MouseWheelEventBridge> by lazy { mutableListOf() }
	private val keyListeners: MutableList<KeyEventBridge> by lazy { mutableListOf() }

	override var backgroundColor: Color = styleProvider.getStyle(StyleType.BACKGROUND).color.backgroundColor

	override val mouseLocation: Point2D
		get() = throw UnsupportedOperationException("getting mouseLocation not yet implemented")

	private val canvas = document.getElementById(id) as HTMLCanvasElement

	private val ctx = canvas.getContext("2d")!! as CanvasRenderingContext2D

	private val g = Graphics2DJs(ctx)

	private var initalizing: Boolean = true

	override val devicePixelRatio: Int = window.devicePixelRatio.toInt()

	override val dimension = Dimension2D(width * devicePixelRatio, height * devicePixelRatio)

	var dragTargetHandler: DragTargetHandler? = null
		set(value) {
			// Using canvas.addEventListener() seems not to work
			field?.let {
				canvas.ondragenter = null
				canvas.ondragover = null
				canvas.ondrop = null
			}
			value?.let { handler ->
				canvas.ondragenter = { event -> handler.onDragEnter(event, windowToCanvas(event)) }
				canvas.ondragover = { event -> handler.onDragOver(event, windowToCanvas(event)) }
				canvas.ondrop = { event -> handler.onDrop(event, windowToCanvas(event)) }
			}
			field = value
		}

	init {
		view.canvas = this

		canvas.width = dimension.width.toInt()
		canvas.height = dimension.height.toInt()

		canvas.style.width = "${width}px"
		canvas.style.height = "${height}px"

		canvas.style.border = "1px solid gray"

		view.initialize()
		initalizing = false
	}

	override fun requestViewFocus() {
		canvas.focus()
	}

	override fun setCursor(cursor: Cursor) {
		when (cursor) {
			Cursor.DEFAULT -> canvas.style.cursor = "default"
			Cursor.WAIT -> canvas.style.cursor = "wait"
			Cursor.CLICK -> canvas.style.cursor = "pointer"
			Cursor.MOVE -> canvas.style.cursor = "move"
			Cursor.CROSSHAIR -> canvas.style.cursor = "crosshair"
			Cursor.NW_RESIZE -> canvas.style.cursor = "nw-resize"
			Cursor.N_RESIZE -> canvas.style.cursor = "n-resize"
			Cursor.NE_RESIZE -> canvas.style.cursor = "ne-resize"
			Cursor.E_RESIZE -> canvas.style.cursor = "e-resize"
			Cursor.SE_RESIZE -> canvas.style.cursor = "se-resize"
			Cursor.S_RESIZE -> canvas.style.cursor = "s-resize"
			Cursor.SW_RESIZE -> canvas.style.cursor = "sw-resize"
			Cursor.W_RESIZE -> canvas.style.cursor = "w-resize"
			Cursor.TEXT -> canvas.style.cursor = "text"
		}
	}

	override fun repaint() {
		repaint(0, 0, canvas.width, canvas.height)
	}

	override fun repaint(x: Int, y: Int, width: Int, height: Int) {
		LOG.trace("CanvasJs.repaint $x,$y,$width,$height")

		// TODO Trying to redraw minimal areas leads to strange alpha channel artefacts
		// As a workaround, redraw entire canvas
		val xx = (0).toDouble()
		val yy = (0).toDouble()
		val ww = (canvas.width).toDouble()
		val hh = (canvas.height).toDouble()

		g.clip.setFrame(xx, yy, ww, hh)

		ctx.save()
		ctx.setTransform(1.0, 0.0, 0.0, 1.0, 0.0, 0.0)

		//ctx.clearRect(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble())
		ctx.fillStyle = "rgba(${backgroundColor.red},${backgroundColor.green}, ${backgroundColor.blue}, 1.0)"
		ctx.fillRect(xx, yy, ww, hh)

		ctx.restore()
		paint()
	}

	override fun addMouseListener(l: MouseListener) {
		var bridge: MouseEventBridge? = mouseEventBridgeOf(l)
		if (bridge == null) {
			bridge = MouseEventBridge(::windowToCanvas, l, canvas)
			canvas.addEventListener("mousedown", bridge)
			canvas.addEventListener("mouseup", bridge)
			canvas.addEventListener("click", bridge)
			canvas.addEventListener("dblclick", bridge)
			mouseListeners.add(bridge)
		}
	}

	override fun removeMouseListener(l: MouseListener) {
		val bridge = mouseEventBridgeOf(l)
		if (bridge != null) {
			canvas.removeEventListener("mousedown", bridge)
			canvas.removeEventListener("mouseup", bridge)
			canvas.removeEventListener("click", bridge)
			canvas.removeEventListener("dblclick", bridge)
			mouseListeners.remove(bridge)
		}
	}

	override fun addMouseMotionListener(l: MouseMotionListener) {
		var bridge: MouseMotionEventBridge? = mouseMotionEventBridgeOf(l)
		if (bridge == null) {
			bridge = MouseMotionEventBridge(::windowToCanvas, l, canvas)
			canvas.addEventListener("mousedown", bridge)
			canvas.addEventListener("mouseup", bridge)
			canvas.addEventListener("mousemove", bridge)
			mouseMotionListeners.add(bridge)
		}
	}

	override fun removeMouseMotionListener(l: MouseMotionListener) {
		val bridge = mouseMotionEventBridgeOf(l)
		if (bridge != null) {
			canvas.removeEventListener("mousedown", bridge)
			canvas.removeEventListener("mouseup", bridge)
			canvas.removeEventListener("mousemove", bridge)
			mouseMotionListeners.remove(bridge)
		}
	}

	override fun addMouseWheelListener(l: MouseWheelListener) {
		var bridge: MouseWheelEventBridge? = mouseWheelEventBridgeOf(l)
		if (bridge == null) {
			bridge = MouseWheelEventBridge(::windowToCanvas, l, canvas)
			canvas.addEventListener("mousewheel", bridge)
			mouseWheelListeners.add(bridge)
		}
	}

	override fun removeMouseWheelListener(l: MouseWheelListener) {
		val bridge = mouseWheelEventBridgeOf(l)
		if (bridge != null) {
			canvas.removeEventListener("mousewheel", bridge)
			mouseWheelListeners.remove(bridge)
		}
	}

	override fun addKeyListener(l: KeyListener) {
		var bridge = keyEventBridgeOf(l)
		if (bridge == null) {
			bridge = KeyEventBridge(l, canvas)
			canvas.addEventListener("keydown", bridge)
			canvas.addEventListener("keyup", bridge)
		}
	}

	override fun removeKeyListener(l: KeyListener) {
		val bridge = keyEventBridgeOf(l)
		if (bridge != null) {
			canvas.removeEventListener("keydown", bridge)
			canvas.removeEventListener("keyup", bridge)
			keyListeners.remove(bridge)
		}
	}

	override fun setToolTipText(text: String?) {
		canvas.title = text ?: ""
	}

	override fun dispatchEvent(e: InputEvent) {
		// TOD Not yet implemented
	}

	/** ---- [CanvasJs] */

	fun paint() {
		if (!initalizing) {
			view.paint(g)
		}
	}

	/** TODO Make part of the [Canvas] interface.*/
	fun startTimerInterval(function: () -> Unit, interval: Int): Int {
		return kotlinx.browser.window.setInterval(function, interval)
	}

	/** TODO Make part of the [Canvas] interface.*/
	fun stopTimerInterval(id: Int) {
		kotlinx.browser.window.clearInterval(id)
	}

	private fun windowToCanvas(event: org.w3c.dom.events.MouseEvent): Point2D {
		val rect = canvas.getBoundingClientRect()
		return Point2D(
			((event.clientX - rect.left) * devicePixelRatio).toInt(),
			((event.clientY - rect.top) * devicePixelRatio).toInt()
		)
	}

	private fun mouseEventBridgeOf(l: MouseListener): MouseEventBridge? =
		mouseListeners.firstOrNull { it.listener === l }

	private fun mouseMotionEventBridgeOf(l: MouseMotionListener): MouseMotionEventBridge? =
		mouseMotionListeners.firstOrNull { it.listener === l }

	private fun mouseWheelEventBridgeOf(l: MouseWheelListener): MouseWheelEventBridge? =
		mouseWheelListeners.firstOrNull { it.listener === l }

	private fun keyEventBridgeOf(l: KeyListener): KeyEventBridge? =
		keyListeners.firstOrNull { it.listener === l }
}

private class MouseEventJs(
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
			modifiers = modifiers or SHIFT_MASK
		}
		if (event.ctrlKey) {
			modifiers = modifiers or CTRL_MASK
		}
		if (event.metaKey) {
			modifiers = modifiers or META_MASK
		}
		if (event.altKey) {
			modifiers = modifiers or ALT_MASK
		}

		return modifiers
	}
}

private class MouseEventBridge(
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

private class MouseMotionEventBridge(
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

private class MouseWheelEventBridge(
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

private class KeyEventJs(
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
		var modifiers: Int = 0
		if (event.shiftKey) {
			modifiers = modifiers or SHIFT_MASK
		}
		if (event.ctrlKey) {
			modifiers = modifiers or CTRL_MASK
		}
		if (event.metaKey) {
			modifiers = modifiers or META_MASK
		}
		if (event.altKey) {
			modifiers = modifiers or ALT_MASK
		}

		return modifiers
	}

	private fun convertEventType(): KeyEventType {
		// TODO Implement properly
		return KeyEventType.PRESSED
	}
}

private class KeyEventBridge(val listener: KeyListener, val canvas: HTMLCanvasElement) : EventListener {

	override fun handleEvent(event: Event) {
		val e = KeyEventJs(canvas, event as org.w3c.dom.events.KeyboardEvent)
		when (event.type) {
			"keydown" -> listener.keyPressed(e)
			"keyup" -> listener.keyReleased(e)
		}
	}
}