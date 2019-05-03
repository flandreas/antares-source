package ch.scorpion.jabbah.base.event

/** These masks are the same as in the corresponding JDK class.*/
const val SHIFT_MASK = 1.shl(6)
const val CTRL_MASK = 1.shl(7)
const val META_MASK = 1.shl(8)
const val ALT_MASK = 1.shl(9)
const val ALT_GRAPH_MASK = 1.shl(13)

/**
 * Abstract cross-platform base class of events that indicate a user input.
 */
interface InputEvent  {

    /** The platform event wrapped by this [InputEvent].*/
    val event: Any?

    /** The component from where the event originates.*/
    val source: Any

    /** The state of the modified mask at the time the [InputEvent] was fired.*/
    val modifiers: Int

    val isAltDown: Boolean get() = (modifiers and ALT_MASK) != 0

    val isControlDown: Boolean get() = (modifiers and CTRL_MASK) != 0

    val isShiftDown: Boolean get() = (modifiers and SHIFT_MASK) != 0

    val isCtrlDown: Boolean get() = (modifiers and CTRL_MASK) != 0

    val isAltGraphDown: Boolean get() = (modifiers and ALT_GRAPH_MASK) != 0

    val isMetaDown: Boolean get() = (modifiers and META_MASK) != 0

    fun consume()

	fun isConsumed(): Boolean
}
