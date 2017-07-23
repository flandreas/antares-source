package ch.scorpion.jabbah.draw.view

import ch.scorpion.jabbah.base.event.*
import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.draw.Canvas
import ch.scorpion.jabbah.draw.InputEventContext
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.Cursor
import ch.scorpion.jabbah.draw.graphics.Graphics2DJs
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.EventListener
import kotlin.browser.document

/**
 * Maps a HTML canvas to the [Canvas] interface.
 */
class CanvasJs(
        id: String,
        viewFactory: (Canvas) -> View<out InputEventContext>,
        styleProvider: StyleProvider
) : Canvas {

    private val LOG by logger()

    private val mouseListeners: MutableList<MouseEventBridge> by lazy {mutableListOf<MouseEventBridge>()}
    private val mouseMotionListeners: MutableList<MouseMotionEventBridge> by lazy {mutableListOf<MouseMotionEventBridge>()}
    private val mouseWheelListeners: MutableList<MouseWheelEventBridge> by lazy {mutableListOf<MouseWheelEventBridge>()}
    private val keyListeners: MutableList<KeyEventBridge> by lazy { mutableListOf<KeyEventBridge>()}

    override var backgroundColor: Color = styleProvider.getStyle(StyleType.BACKGROUND).color.backgroundColor

    override val view: View<out InputEventContext>

    private val canvas = document.getElementById(id) as HTMLCanvasElement

    private val ctx = canvas.getContext("2d")!! as CanvasRenderingContext2D

    private val g = Graphics2DJs(ctx)

    private var initalizing: Boolean = true

    override val dimension: Dimension2D
        get() = Dimension2D(canvas.width.toDouble(), ctx.canvas.height.toDouble())

    init {
        view = viewFactory.invoke(this)
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
            Cursor.HAND -> canvas.style.cursor = "move"
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
        LOG.debug("CanvasJs.repaint $x,$y,$width,$height")

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
            bridge = MouseEventBridge(l, canvas)
            canvas.addEventListener("mousedown", bridge)
            canvas.addEventListener("mouseup", bridge)
            canvas.addEventListener("click", bridge)
            canvas.addEventListener("dblclick", bridge)
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
            bridge = MouseMotionEventBridge(l, canvas)
            canvas.addEventListener("mousedown", bridge)
            canvas.addEventListener("mouseup", bridge)
            canvas.addEventListener("mousemove", bridge)
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
            bridge = MouseWheelEventBridge(l, canvas)
            canvas.addEventListener("mousewheel", bridge)
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
        // TODO Tooltip not yet supported
    }

    /** ---- [CanvasJs] */

    fun paint() {
        if (!initalizing) {
            view.paint(g)
        }
    }

    /** TODO Make part of the [Canvas] interface.*/
    fun startTimerInterval(function: () -> Unit, interval: Int): Int {
        return kotlin.browser.window.setInterval(function, interval)
    }

    /** TODO Make part of the [Canvas] interface.*/
    fun stopTimerInterval(id: Int) {
        kotlin.browser.window.clearInterval(id)
    }

    private fun mouseEventBridgeOf(l: MouseListener): MouseEventBridge? {
        return mouseListeners.filter { it.listener === l }.firstOrNull()
    }

    private fun mouseMotionEventBridgeOf(l: MouseMotionListener): MouseMotionEventBridge? {
        return mouseMotionListeners.filter { it.listener === l }.firstOrNull()
    }

    private fun mouseWheelEventBridgeOf(l: MouseWheelListener): MouseWheelEventBridge? {
        return mouseWheelListeners.filter { it.listener === l }.firstOrNull()
    }

    private fun keyEventBridgeOf(l: KeyListener): KeyEventBridge? {
        return keyListeners.filter { it.listener === l }.firstOrNull()
    }
}

private class MouseEventJs(private val canvas: HTMLCanvasElement, private val event: org.w3c.dom.events.MouseEvent) : MouseEvent {

    override val source: Any get() = canvas

    override val button: Button get() = convertButton()

    override val clickCount: Int get() =if(event.type == "dblclick") 2 else 1

    override val modifiers: Int get() = convertModifiers()

    override val wheelRotation: Int
        get() = if (event is org.w3c.dom.events.WheelEvent) event.deltaY.toInt() else 0

    override val x: Int get() = windowToCanvasX(event.clientX)

    override val y: Int get() = windowToCanvasY(event.clientY)

    override fun consume() {
        event.preventDefault()
    }

    private fun convertButton(): Button {
        return when(event.button.toInt()) {
            0 -> Button.BUTTON1
            1 -> Button.BUTTON2
            2 -> Button.BUTTON3
            else -> Button.NONE
        }
    }

    private fun convertModifiers(): Int {
        var modifiers: Int = 0
        if (event.shiftKey) {
            modifiers = modifiers or ch.scorpion.jabbah.base.event.SHIFT_MASK
        }
        if (event.ctrlKey) {
            modifiers = modifiers or ch.scorpion.jabbah.base.event.CTRL_MASK
        }
        if (event.metaKey) {
            modifiers = modifiers or ch.scorpion.jabbah.base.event.META_MASK
        }
        if (event.altKey) {
            modifiers = modifiers or ch.scorpion.jabbah.base.event.ALT_MASK
        }

        return modifiers
    }

    fun windowToCanvasX(x: Int): Int {
        val bbox = canvas.getBoundingClientRect()
        return (x - bbox.left * (canvas.width / bbox.width)).toInt()
    }

    fun windowToCanvasY(y: Int): Int {
        val bbox = canvas.getBoundingClientRect()
        return (y - bbox.top * (canvas.height / bbox.height)).toInt()
    }
}

private class MouseEventBridge(val listener: MouseListener, private val canvas: HTMLCanvasElement)
    : EventListener {

    override fun handleEvent(event: Event) {
        val e = MouseEventJs(canvas, event as org.w3c.dom.events.MouseEvent)
        when(event.type) {
            "mousedown" -> listener.mousePressed(e)
            "mouseup" -> listener.mouseReleased(e)
            "click" -> listener.mouseClicked(e)
            "dblclick" -> listener.mouseClicked(e)
        }
    }
}

private class MouseMotionEventBridge(val listener: MouseMotionListener, private val canvas: HTMLCanvasElement)
    : EventListener {

    private var pressed: Boolean = false

    override fun handleEvent(event: Event) {
        val e = MouseEventJs(canvas, event as org.w3c.dom.events.MouseEvent)
        when (event.type) {
            "mousedown" -> { pressed = true }
            "mouseup" -> { pressed = false }
            "mousemove" -> { if (pressed) { listener.mouseDragged(e) } else { listener.mouseMoved(e) }
            }
        }
    }
}

private class MouseWheelEventBridge(val listener: MouseWheelListener, private val canvas: HTMLCanvasElement)
    : EventListener {

    override fun handleEvent(event: Event) {
        val w3cEvent = event as org.w3c.dom.events.WheelEvent
        val e = MouseEventJs(canvas, w3cEvent)
        listener.mouseWheelRotated(e)
    }
}

private class KeyEventJs(private val canvas: HTMLCanvasElement, private val event: org.w3c.dom.events.KeyboardEvent) : KeyEvent {

    override val key: Int get() = event.keyCode

    override val source: Any get() = canvas

    override val modifiers: Int get() = convertModifiers()

    override fun consume() {
        event.preventDefault()
    }

    private fun convertModifiers(): Int {
        var modifiers: Int = 0
        if (event.shiftKey) {
            modifiers = modifiers or ch.scorpion.jabbah.base.event.SHIFT_MASK
        }
        if (event.ctrlKey) {
            modifiers = modifiers or ch.scorpion.jabbah.base.event.CTRL_MASK
        }
        if (event.metaKey) {
            modifiers = modifiers or ch.scorpion.jabbah.base.event.META_MASK
        }
        if (event.altKey) {
            modifiers = modifiers or ch.scorpion.jabbah.base.event.ALT_MASK
        }

        return modifiers
    }
}

private class KeyEventBridge(val listener: KeyListener, val canvas: HTMLCanvasElement) : EventListener {

    override fun handleEvent(event: Event) {
        val e = KeyEventJs(canvas, event as org.w3c.dom.events.KeyboardEvent)
        when(event.type) {
            "keydown" -> listener.keyPressed(e)
            "keyup" -> listener.keyReleased(e)
        }
    }
}