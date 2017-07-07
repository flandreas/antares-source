package ch.scorpion.jabbah.draw

/**
 * Handles mouse and key inputs that are targeted at a [Drawable] in a [View].
 *
 * All [InputEventHandler] methods return the [InputEventHandler] that should receive the next mouse input,
 * or `null` if the next recipient cannot be determined.
 */
interface InputEventHandler<in T : InputEventContext> {

    fun mouseClicked(context: T): InputEventHandler<T>?

    fun mouseMoved(context: T): InputEventHandler<T>?

    fun mousePressed(context: T): InputEventHandler<T>?

    fun mouseDragged(context: T): InputEventHandler<T>?

    fun mouseReleased(context: T): InputEventHandler<T>?

    fun keyPressed(context: T): InputEventHandler<T>?

    fun keyReleased(context: T): InputEventHandler<T>?
}

/**
 * An empty implementation of the [InputEventHandler] interface whose methods do nothing and return `null`,
 * or forward to an optional successor being part of a chain of responsibility.
 *
 * @param successor the optional successor to which events are forwarded
 */
open class InputEventHandlerAdapter<in T : InputEventContext>(
    val successor: InputEventHandler<T>? = null
) : InputEventHandler<T> {

    companion object {
        val EMPTY_HANDLER = InputEventHandlerAdapter<InputEventContext>()
    }

    override fun mouseClicked(context: T): InputEventHandler<T>? {
        return successor?.mouseClicked(context)
    }

    override fun mouseMoved(context: T): InputEventHandler<T>? {
        return successor?.mouseMoved(context)
    }

    override fun mousePressed(context: T): InputEventHandler<T>? {
        return successor?.mousePressed(context)
    }

    override fun mouseDragged(context: T): InputEventHandler<T>? {
        return successor?.mouseDragged(context)
    }

    override fun mouseReleased(context: T): InputEventHandler<T>? {
        return successor?.mouseReleased(context)
    }

    override fun keyPressed(context: T): InputEventHandler<T>? {
        return successor?.keyPressed(context)
    }

    override fun keyReleased(context: T): InputEventHandler<T>? {
        return successor?.keyReleased(context)
    }
}
