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
interface MouseEvent : InputEvent {
    val x: Int
    val y: Int
    val button: Button
    val clickCount: Int
    val wheelRotation: Int
    val location: Point2D get() = Point2D(x.toDouble(), y.toDouble())
}

/** A platform-independent empty implementation of [MouseEvent] to be used when applying the "null pattern".*/
class EmptyMouseEvent : MouseEvent {

    override val event: Any get() = this
    override val x: Int get() = 0
    override val y: Int get() = 0
    override val button: Button get() = Button.NONE
    override val clickCount: Int get() = 0
    override val wheelRotation: Int get() = 0
    override val source: Any get() = this
    override val modifiers: Int get() = 0

    override fun consume() {
        // empty
    }

	override fun isConsumed(): Boolean {
		return false
	}
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
