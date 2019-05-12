package ch.scorpion.jabbah.edit

/**
 * Manages focus handling on the [Component] level. This is not the same as focus management on
 * the level of the windowing system. [Component]s are displayed within a [DrawingView], which is
 * contained in a single object of the windowing system, such as a canvas. Hence, this [FocusManager]
 * performs a kind of sub-focus management.
 *
 * [Component] focus management is only active when in execution mode.
 */
object FocusManager {

    /** The [Component] that currently has the focus, if any.*/
    var focusOwner: Component? = null
        private set(value) {
            if (field == value) {
                return
            }
            if (value != null && !value.isFocusable) {
                return
            }
            val oldValue = field
            field = value
            oldValue?.focusLost()
            field?.focusGained()
        }

    /** Transfers the focus to the next focusable [Component] of the currently active [DrawingView].*/
    fun transferFocus() {
        // TODO
    }

    fun requestFocus(c: Component) {
        focusOwner = c
    }

	fun resetFocus() {
		focusOwner = null
	}
}