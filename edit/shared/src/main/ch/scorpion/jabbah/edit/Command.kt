package ch.scorpion.jabbah.edit

import ch.scorpion.jabbah.draw.View
/**
 * A [Command] encapsulates a change of a [Drawing]'s state in order to make the change undoable and
 * redoable.
 */
interface Command {

    /**
     * Returns a description of what this [Command] does, for example "Move".
     *
     * The description is intended to be used for menu items of actions that allows the user to undo and redo the
     * [Command]. The text of those menu items could include this description to give the user a hint of what he
     * would get when executing the undo or redo actions.
     *
     * The returned description should be displayable and internationalized.
     * @return a description of what this [Command] does.
     */
    fun getDescription(): String

    /**
     * Executes the change of a [Drawing]'s state that is associated with this [Command].
     *
     * This method is called by the [CommandManager] to reset the effect of a previous undo action. But it can
     * also be called by the code that created this [Command], thus avoiding duplicate code for changing the state
     * of the [Drawing].
     */
    fun execute()

    /**
     * Notifies this [Command] that it has been registered with a [CommandManager] without execution.
     * Implementations can update their state in order to prepare for future undo and re-execution if necessary.
     */
    fun registered()

    /**
     * Undoes the change of an [Drawing]'s state that is associated with this [Command].
     *
     * This method is called by the [CommandManager] to perform an undo action.
     */
    fun undo()

    /**
     * Called by the [CommandManager] in order to validate any [View] that displays the changed
     * [Drawing] after [execute] or [undo] has been done.
     */
    fun validate()
}