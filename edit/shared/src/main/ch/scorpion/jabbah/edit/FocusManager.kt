package ch.scorpion.jabbah.edit

interface Focusable {

	/** Controls whether this [Focusable] can receive the focus.*/
	val isFocusable: Boolean

	/** Determines whether this [Focusable] currently has the focus.*/
	val isFocusOwner: Boolean get() = FocusManager.focusOwner === this

	/** Requests the focus for this [Focusable].*/
	fun requestFocus() = FocusManager.requestFocus(this)

	/**
	 * Informs this [Focusable] that it has gained the focus. Implementing classes should update their
	 * graphical representation. This method is typically only called by the [FocusManager].
	 */
	fun focusGained() { }

	/**
	 * Informs this [Component] that it has lost the focus. Implementing classes should update their
	 * graphical representation.  This method is typically only called by the [FocusManager].
	 */
	fun focusLost() { }
}

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
    var focusOwner: Focusable? = null
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

    fun requestFocus(c: Focusable) {
        focusOwner = c
    }

	fun resetFocus() {
		focusOwner = null
	}
}