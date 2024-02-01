package ch.scorpion.jabbah.base.event

import kotlin.js.JsExport

/**
 * These masks are the same as in the corresponding JDK class. Note that [mask] is writable
 * in order to adjusted for the current platform, particularly "meta", which is "CMD" on MacOS
 * and "CTRL" on Windows, both with different masks.
 */
enum class Modifier(val label: String, var mask: Int) {
	Shift("Shift", 1.shl(6)),
	Ctrl("Ctrl", 1.shl(7)),
	Meta("Meta", 1.shl(8)),
	Alt("Alt", 1.shl(9)),
	AltGraph("AltGr", 1.shl(13))
}

/**
 * Abstract cross-platform base class of events that indicate a user input.
 */
@JsExport
interface InputEvent  {

    /** The platform event wrapped by this [InputEvent].*/
    val event: Any?

    /** The component from where the event originates.*/
    val source: Any

    /** The state of the modified mask at the time the [InputEvent] was fired.*/
    val modifiers: Int

    val isAltDown: Boolean get() = (modifiers and Modifier.Alt.mask) != 0

    val isControlDown: Boolean get() = (modifiers and Modifier.Ctrl.mask) != 0

    val isShiftDown: Boolean get() = (modifiers and Modifier.Shift.mask) != 0

    val isCtrlDown: Boolean get() = (modifiers and Modifier.Ctrl.mask) != 0

    val isAltGraphDown: Boolean get() = (modifiers and Modifier.AltGraph.mask) != 0

    val isMetaDown: Boolean get() = (modifiers and Modifier.Meta.mask) != 0

	fun hasModifier(modifier: Modifier): Boolean = (modifiers and modifier.mask) != 0

    /** Note: The method name "consume()" gets mangled on JS platform.*/
    fun consumeEvent()

	fun isConsumed(): Boolean
}
