package ch.scorpion.jabbah.draw

import ch.scorpion.jabbah.base.event.KeyEvent

interface Focusable {

	/** Controls whether this [Focusable] can receive the focus.*/
	val isFocusable: Boolean

	/** Determines whether this [Focusable] currently has the focus.*/
	val isFocusOwner: Boolean get() = FocusManager.focusOwner === this

    /**
     * Returns `true` if this [Focusable] can consume the specified [KeyEvent].
     * Used for deciding whether top-level window accelerator key events should be intercepted
     * by [Focusable]s having the current focus. By default, this is only true if [keyEvent]
     * doesn't have a modifier such as CTRL set.
     * Subclasses might overwrite and fine-tune this behaviour.
     */
    fun canConsume(keyEvent: KeyEvent): Boolean = keyEvent.modifiers == 0

	/** Requests the focus for this [Focusable].*/
	fun requestFocus() = FocusManager.requestFocus(this)

	/**
	 * Informs this [Focusable] that it has gained the focus. Implementing classes should update their
	 * graphical representation. This method is typically only called by the [FocusManager].
	 */
	fun focusGained() { }

	/**
	 * Informs this [Focusable] that it has lost the focus. Implementing classes should update their
	 * graphical representation.  This method is typically only called by the [FocusManager].
	 */
	fun focusLost() { }
}

/**
 * Manages focus handling on the [Drawable] level. This is not the same as focus management on
 * the level of the windowing system. [Drawable]s are displayed within a [View], which is
 * contained in a single object of the windowing system, such as a canvas. Hence, this [FocusManager]
 * performs a kind of sub-focus management.
 *
 * [Drawable] focus management is only active when in execution mode.
 */
object FocusManager {

    /** The [Drawable] that currently has the focus, if any.*/
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

    /** Transfers the focus to the next focusable [Drawable] of the currently active [View].*/
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