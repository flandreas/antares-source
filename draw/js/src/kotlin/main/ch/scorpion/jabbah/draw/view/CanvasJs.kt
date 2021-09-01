package ch.scorpion.jabbah.draw.view

import ch.scorpion.jabbah.base.event.*
import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.Canvas
import ch.scorpion.jabbah.draw.InputEventContext
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.Cursor
import ch.scorpion.jabbah.draw.graphics.Graphics2DJs
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement

class CanvasJs(
	id: String,
	override val view: View<out InputEventContext>,
	size: Dimension2D?,
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	private val propertyOwner: PropertyOwner<Any> = PropertyOwnerImpl()
) : Canvas, PropertyOwner<Any> by propertyOwner {

	private val canvas = document.getElementById(id) as HTMLCanvasElement

	private val ctx = canvas.getContext("2d")!! as CanvasRenderingContext2D

	private val g = Graphics2DJs(ctx, window.devicePixelRatio)

	private var initalizing: Boolean = true

	private val mouseListeners: MutableList<MouseEventBridge> by lazy { mutableListOf() }
	private val mouseMotionListeners: MutableList<MouseMotionEventBridge> by lazy { mutableListOf() }
	private val mouseWheelListeners: MutableList<MouseWheelEventBridge> by lazy { mutableListOf() }
	private val keyListeners: MutableList<KeyEventBridge> by lazy { mutableListOf() }

	/** ---- [Canvas] interface */

	override val devicePixelRatio: Int get() = window.devicePixelRatio.toInt()

	override val dimension: Dimension2D = size ?: Dimension2D(canvas.offsetWidth, canvas.offsetHeight)

	override var backgroundColor: Color = styleProvider.getStyle(StyleType.BACKGROUND).color.backgroundColor

	override val mouseLocation: Point2D
		get() = throw UnsupportedOperationException("getting mouseLocation not yet implemented")

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
		propertyOwner.source = this
		view.canvas = this
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

	override fun setToolTipText(text: String?) { }

	override fun dispatchEvent(e: InputEvent) { }

	override fun fire(name: String, oldValue: Any?, newValue: Any?) { }

	/** ---- [CanvasJs] */

	private fun paint() {
		if (!initalizing) {
			view.paint(g)
		}
	}

	private fun windowToCanvas(event: org.w3c.dom.events.MouseEvent): Point2D {
		val rect = canvas.getBoundingClientRect()
		return Point2D(
			((event.clientX - rect.left)).toInt(),
			((event.clientY - rect.top)).toInt()
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