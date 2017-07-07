package ch.scorpion.jabbah.draw.view

import ch.scorpion.jabbah.base.event.*
import ch.scorpion.jabbah.base.exception.IllegalArgumentException
import ch.scorpion.jabbah.draw.Canvas
import ch.scorpion.jabbah.draw.InputEventContext
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.Cursor
import ch.scorpion.jabbah.draw.style.Style
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.draw.graphics.Graphics2DJvm
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.event.MouseWheelEvent
import javax.swing.JPanel
import javax.swing.JComponent
import java.awt.event.MouseEvent as JvmMouseEvent
import java.awt.event.MouseWheelEvent as JvmMouseWheelEvent
import java.awt.event.KeyEvent as JvmKeyEvent

/**
 * Implements the [Canvas] interface on the JVM platform as a [JPanel].
 *
 * @param viewFactory the factory that provides the [View] to be displayed by this [Canvas]
 * @param styleProvider provides the [Style] that yields the background color of this [Canvas]
 */
class CanvasJvm(
        viewFactory: (Canvas) -> View<out InputEventContext>,
        styleProvider: StyleProvider
) : JPanel(), Canvas {

    constructor(viewFactory: (Canvas) -> View<out InputEventContext>): this(viewFactory, DrawStyleModule.styleProvider)

    private val mouseListeners: MutableList<MouseEventBridge> by lazy {mutableListOf<MouseEventBridge>()}
    private val mouseMotionListeners: MutableList<MouseMotionEventBridge> by lazy {mutableListOf<MouseMotionEventBridge>()}
    private val mouseWheelListeners: MutableList<MouseWheelEventBridge> by lazy { mutableListOf<MouseWheelEventBridge>()}
    private val keyListeners: MutableList<KeyEventBridge> by lazy { mutableListOf<KeyEventBridge>()}

    override val view: View<*>

    init {
        val color = styleProvider.getStyle(StyleType.BACKGROUND).color.backgroundColor
        background = java.awt.Color(color.red, color.green, color.blue, color.alpha)
        layout = null
        view = viewFactory.invoke(this)
        view.initialize()
    }

    override var backgroundColor: Color
        get() = Color(background.red, background.green, backgroundColor.blue, backgroundColor.alpha)
        set(value) {
            background = java.awt.Color(value.red, value.green, value.blue, value.alpha)
        }

    override val dimension: Dimension2D
        get() = Dimension2D(width.toDouble(), height.toDouble())

    override fun requestViewFocus() {
        super.requestFocusInWindow()
    }

    override fun setCursor(cursor: Cursor) {
        when (cursor) {
            Cursor.DEFAULT -> setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR))
            Cursor.WAIT -> setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR))
            Cursor.HAND -> setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR))
            Cursor.CROSSHAIR -> setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.CROSSHAIR_CURSOR))
            Cursor.NW_RESIZE -> setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.NW_RESIZE_CURSOR))
            Cursor.N_RESIZE -> setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.N_RESIZE_CURSOR))
            Cursor.NE_RESIZE -> setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.NE_RESIZE_CURSOR))
            Cursor.E_RESIZE -> setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.E_RESIZE_CURSOR))
            Cursor.SE_RESIZE -> setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.SE_RESIZE_CURSOR))
            Cursor.S_RESIZE -> setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.S_RESIZE_CURSOR))
            Cursor.SW_RESIZE -> setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.SW_RESIZE_CURSOR))
            Cursor.W_RESIZE -> setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.W_RESIZE_CURSOR))
            Cursor.TEXT -> setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.TEXT_CURSOR))
        }
    }

    override fun addMouseListener(l: MouseListener) {
        var bridge = mouseEventBridgeOf(l)
        if (bridge == null) {
            bridge = MouseEventBridge(l)
            mouseListeners.add(bridge)
            super.addMouseListener(bridge)
        }
    }

    override fun removeMouseListener(l: MouseListener) {
        val bridge = mouseEventBridgeOf(l)
        if (bridge != null) {
            mouseListeners.remove(bridge)
            super.removeMouseListener(bridge)
        }
    }

    override fun addMouseMotionListener(l: MouseMotionListener) {
        var bridge = mouseMotionEventBridgeOf(l)
        if (bridge == null) {
            bridge = MouseMotionEventBridge(l)
            mouseMotionListeners.add(bridge)
            super.addMouseMotionListener(bridge)
        }
    }

    override fun removeMouseMotionListener(l: MouseMotionListener) {
        val bridge = mouseMotionEventBridgeOf(l)
        if (bridge != null) {
            mouseMotionListeners.remove(bridge)
            super.removeMouseMotionListener(bridge)
        }
    }

    override fun addMouseWheelListener(l: MouseWheelListener) {
        var bridge = mouseWheelEventBridgeOf(l)
        if (bridge == null) {
            bridge = MouseWheelEventBridge(l)
            mouseWheelListeners.add(bridge)
            super.addMouseWheelListener(bridge)
        }
    }

    override fun removeMouseWheelListener(l: MouseWheelListener) {
        val bridge = mouseWheelEventBridgeOf(l)
        if (bridge != null) {
            mouseWheelListeners.remove(bridge)
            super.removeMouseWheelListener(bridge)
        }
    }

    override fun addKeyListener(l: KeyListener) {
        var bridge = keyEventBridgeOf(l)
        if (bridge == null) {
            bridge = KeyEventBridge(l)
            keyListeners.add(bridge)
            super.addKeyListener(bridge)
        }
    }

    override fun removeKeyListener(l: KeyListener) {
        val bridge = keyEventBridgeOf(l)
        if (bridge != null) {
            keyListeners.remove(bridge)
            super.removeKeyListener(bridge)
        }
    }

    /** ---- [JComponent] */

    override fun paintComponent(g: Graphics?) {
        super.paintComponent(g)
        view.paint(Graphics2DJvm(g as Graphics2D))
    }

    /** ---- [CanvasJvm] */

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


private abstract class AbstractMouseEventBridge {

    fun convertEvent(jvmEvent: JvmMouseEvent): MouseEvent {
        return MouseEvent(
                source = jvmEvent.component,
                modifiers = jvmEvent.modifiers,
                x = jvmEvent.x,
                y = jvmEvent.y,
                button = convertButton(jvmEvent.button),
                clickCount = jvmEvent.clickCount
        )
    }

    fun convertEvent(jvmEvent: JvmMouseWheelEvent): MouseEvent {
        return MouseEvent(
                source = jvmEvent.component,
                modifiers = jvmEvent.modifiers,
                x = jvmEvent.x,
                y = jvmEvent.y,
                button = convertButton(jvmEvent.button),
                wheelRotation = jvmEvent.wheelRotation
        )
    }

    fun convertButton(jvmButton: Int): Button {
        return when(jvmButton) {
            JvmMouseEvent.NOBUTTON -> Button.NONE
            JvmMouseEvent.BUTTON1 -> Button.BUTTON1
            JvmMouseEvent.BUTTON2 -> Button.BUTTON2
            JvmMouseEvent.BUTTON3 -> Button.BUTTON3
            else -> throw IllegalArgumentException("unknown button $jvmButton")
        }
    }
}

private class KeyEventBridge(val listener: KeyListener) : java.awt.event.KeyListener {

    override fun keyTyped(e: JvmKeyEvent?) {
        // empty
    }

    override fun keyPressed(e: JvmKeyEvent?) {
        if (e != null) {
            listener.keyPressed(convertEvent(e))
        }
    }

    override fun keyReleased(e: JvmKeyEvent?) {
        if (e != null) {
            listener.keyReleased(convertEvent(e))
        }
    }

    private fun convertEvent(jvmEvent: JvmKeyEvent): KeyEvent {
        return KeyEvent(
                source = jvmEvent.component,
                modifiers = jvmEvent.modifiers,
                key = jvmEvent.keyCode
        )
    }
}

private class MouseMotionEventBridge(val listener: MouseMotionListener) : AbstractMouseEventBridge(), java.awt.event.MouseMotionListener {

    override fun mouseMoved(e: JvmMouseEvent?) {
        if (e != null) {
            listener.mouseMoved(convertEvent(e))
        }
    }

    override fun mouseDragged(e: JvmMouseEvent?) {
        if (e != null) {
            listener.mouseDragged(convertEvent(e))
        }
    }
}

private class MouseWheelEventBridge(val listener: MouseWheelListener) : AbstractMouseEventBridge(), java.awt.event.MouseWheelListener {

    override fun mouseWheelMoved(e: MouseWheelEvent?) {
        if (e != null) {
            listener.mouseWheelRotated(convertEvent(e))
        }
    }
}

private class MouseEventBridge(val listener: MouseListener) : AbstractMouseEventBridge(), java.awt.event.MouseListener {

    override fun mouseEntered(e: JvmMouseEvent?) {
        if (e != null) {
            listener.mouseEntered(convertEvent(e))
        }
    }

    override fun mouseClicked(e: JvmMouseEvent?) {
        if (e != null) {
            listener.mouseClicked(convertEvent(e))
        }
    }

    override fun mouseReleased(e: JvmMouseEvent?) {
        if (e != null) {
            listener.mouseReleased(convertEvent(e))
        }
    }

    override fun mouseExited(e: JvmMouseEvent?) {
        if (e != null) {
            listener.mouseExited(convertEvent(e))
        }
    }

    override fun mousePressed(e: JvmMouseEvent?) {
        if (e != null) {
            listener.mousePressed(convertEvent(e))
        }
    }
}
