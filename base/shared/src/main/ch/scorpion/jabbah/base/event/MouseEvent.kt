package ch.scorpion.jabbah.base.event

import ch.scorpion.jabbah.base.geom.Point2D

/**
 * Identifies the mouse button that issued a [MouseEvent].
 */
enum class Button {
    NONE, BUTTON1, BUTTON2, BUTTON3
}

/**
 * An event which indicates that a mouse action has occurred in a component.
 */
data class MouseEvent(
    override val source: Any,
    override val modifiers: Int = 0,
    val x: Int = 0,
    val y: Int = 0,
    val button: Button = Button.NONE,
    val clickCount: Int = 0,
    val wheelRotation: Int = 0
    ) : InputEvent {
    val location: Point2D get() = Point2D(x.toDouble(), y.toDouble())
    constructor(source: Any, modifiers: Int, x: Int, y: Int): this(source, modifiers, x, y, Button.NONE)
}

interface MouseListener {
    fun mouseClicked(e: MouseEvent)
    fun mousePressed(e: MouseEvent)
    fun mouseReleased(e: MouseEvent)
    fun mouseEntered(e: MouseEvent)
    fun mouseExited(e: MouseEvent)
}

interface MouseMotionListener {
    fun mouseDragged(e: MouseEvent)
    fun mouseMoved(e: MouseEvent)
}

interface MouseWheelListener {
    fun mouseWheelRotated(e: MouseEvent)
}

/**
 * An empty implementation of the [MouseListener] interface intended to be subclassed by classes that
 * only need to implement some of the [MouseListener] event handling methods.
 */
open class MouseAdapter : MouseListener, MouseMotionListener, MouseWheelListener {
    override fun mouseClicked(e: MouseEvent) {}
    override fun mousePressed(e: MouseEvent) {}
    override fun mouseReleased(e: MouseEvent) {}
    override fun mouseEntered(e: MouseEvent) {}
    override fun mouseExited(e: MouseEvent) {}
    override fun mouseDragged(e: MouseEvent) {}
    override fun mouseMoved(e: MouseEvent) {}
    override fun mouseWheelRotated(e: MouseEvent) {}
}
