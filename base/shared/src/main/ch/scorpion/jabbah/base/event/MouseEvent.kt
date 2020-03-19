package ch.scorpion.jabbah.base.event

import ch.scorpion.jabbah.base.geom.Point2D

/**
 * Identifies the mouse button that issued a [MouseEvent].
 */
enum class Button {
    NONE, BUTTON1, BUTTON2, BUTTON3
}

enum class MouseEventType {
	CLICKED, PRESSED, RELEASED, ENTERED, EXITED, MOVED, DRAGGED, WHEEL_ROTATED
}

/**
 * An event which indicates that a mouse action has occurred in a component.
 */
interface MouseEvent : InputEvent {
	val type: MouseEventType
    val x: Int
    val y: Int
    val button: Button
    val clickCount: Int
    val wheelRotation: Int
    val location: Point2D get() = Point2D(x.toDouble(), y.toDouble())
	val isLeftButtonDown: Boolean
	val isMiddleButtonDown: Boolean
	val isRightButtonDown: Boolean
}

/**
 * A platform-independent empty implementation of [MouseEvent] to be used when applying the "null pattern",
 * or for testing.
 */
data class MouseEventImpl(
	override val type: MouseEventType = MouseEventType.PRESSED,
	override val event: Any = "",
	override val x: Int = 0,
	override val y: Int = 0,
	override val button: Button = Button.NONE,
	override val clickCount: Int = 0,
	override val wheelRotation: Int = 0,
	override val source: Any = "",
	override val modifiers: Int = 0
) : MouseEvent {

	private var consumed: Boolean = false

	override fun consume() {
		consumed = true
	}

	override fun isConsumed(): Boolean = consumed

	override fun toString(): String = "Mouse.$type at $x,$y"

	override val isLeftButtonDown: Boolean get() = button == Button.BUTTON1

	override val isMiddleButtonDown: Boolean get() = button == Button.BUTTON2

	override val isRightButtonDown: Boolean get() = button == Button.BUTTON3
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
