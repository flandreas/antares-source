package ch.scorpion.jabbah.edit

import ch.scorpion.jabbah.draw.View

/**
 * Optionally enhances [Command] with an undo operation.
 * This can be implemented by [Command]s with a simple, straight-forward execute logic that is easy to undo.
 * If a [Command] implements [Undoable], [CommandManager] implementations that use snapshots can omit
 * applying snapshots and replaying long lists of [Command]s in favour of performance.
 */
interface Undoable {

	/**
	 * Determines whether this [Undoable] can undo an operation. While this will be `true` for most
	 * [Undoable] implementations, composite [Command]s will have to ask all their children as well.
	 */
	val canUndo: Boolean get() = true

	/**
	 * An operation that undoes some other operation.
	 * TODO Rename to "undo" once [Command.undo] has been removed.
	 */
	fun undo1()
}

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