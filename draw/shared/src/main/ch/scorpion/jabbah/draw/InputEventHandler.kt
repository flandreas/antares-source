package ch.scorpion.jabbah.draw

import ch.scorpion.jabbah.base.state.StateMachine

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

/**
 * Implements [InputEventHandler] logic using the specified non-strict [StateMachine],
 * forwarding unhandled events to an optional successor [InputEventHandler].
 */
class StateMachineInputEventHandler<T : InputEventContext>(
	val sm: StateMachine<T>,
	val successor: InputEventHandler<T>? = null
) : InputEventHandler<T>{

	init {
		if (sm.strict)  {
			throw IllegalArgumentException("StateMachine must not be strict")
		}
	}

	override fun mouseClicked(context: T): InputEventHandler<T>? {
		return if (sm.handle(context)) this else successor?.mouseClicked(context)
	}

	override fun mouseMoved(context: T): InputEventHandler<T>? {
		return if (sm.handle(context)) this else successor?.mouseMoved(context)
	}

	override fun mousePressed(context: T): InputEventHandler<T>? {
		return if (sm.handle(context)) this else successor?.mousePressed(context)
	}

	override fun mouseDragged(context: T): InputEventHandler<T>? {
		return if (sm.handle(context)) this else successor?.mouseDragged(context)
	}

	override fun mouseReleased(context: T): InputEventHandler<T>? {
		return if (sm.handle(context)) this else successor?.mouseReleased(context)
	}

	override fun keyPressed(context: T): InputEventHandler<T>? {
		return if (sm.handle(context)) this else successor?.keyPressed(context)
	}

	override fun keyReleased(context: T): InputEventHandler<T>? {
		return if (sm.handle(context)) this else successor?.keyReleased(context)
	}
}

