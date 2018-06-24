package ch.scorpion.jabbah.edit

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.exception.IllegalStateException

/**
 * Manages a list of [Command]s that can be played back and forth to support undoability and redoablity of these
 * [Command]s.
 *
 * Posts a [CommandEvent] on a [CommandManager]'s [EventBus] whenever a [Command] has been
 * registered, executed or undone.
 *
 * A [CommandManager] resets its state after the current application data have been saved.
 */
interface CommandManager {

    /** The [EventBus] used by this [CommandManager].*/
    val eventBus: EventBus

	/** Determines whether this [CommandManager] contains any undoable [Command] with propert [Command.changesApplicationData] being set.*/
    val applicationDataChanged: Boolean

    /**
     * Begins a new transaction with the specified [Command] as its head, and executes or registers the [Command]
     * depending on the [register] property.
     *
     * @param register `true` if [command] should only be registered and not executed. If omitted,
     * the [Command] is executed
     * @throws IllegalStateException if there is already a current transaction, since nested transactions
     * are not supported
     */
    fun beginTransaction(command: Command, register: Boolean = false)

    /**
     * Begins a new transaction by creating a dummy [Command] with the specified description.
     * @param descriptionKey the translation key of the transaction's description
     * @property drawingView the [DrawingView] to validate, if any
     */
    fun beginTransaction(descriptionKey: String, drawingView: DrawingView<*>? = null)

    /**
     * Executes the specified [Command] and registers it with the current transaction.
     * @throws IllegalStateException if there is no current transaction
     */
    fun execute(command: Command)

    /**
     * Registers the specified [Command] with the current transaction without executing it.
     * @throws IllegalStateException if there is no current transaction
     */
    fun register(command: Command)

    /**
     * Commits the current transaction by adding it to the undo stack.
     * Posts a [CommandEvent] after execution.
     * @throws IllegalStateException if there is no current transaction
     */
    fun commitTransaction()

    /**
     * Rollbacks the current transaction by undoing all its [Command] and deleting the transaction.
     */
    fun rollbackTransaction()

    /** Determines whether there is a transaction in the undo stack that can be undone. */
    fun canUndo(): Boolean

    /** Determines whether there is a transaction in the redo stack that can be redone.*/
    fun canRedo(): Boolean

    /** Returns the description of the next transaction in the undo stack, if any.*/
    fun getUndoDescription(): String?

    /** Returns the description of the next transaction in the redo stack, if any.*/
    fun getRedoDescription(): String?

    /**
     * Undoes the last transaction in the undo stack by calling the [Command.undo] method of all its [Command]
     * in reverse order.
     *
     * This method validates the [Drawing] on which the undone head [Command] operates.
     * Posts a [CommandEvent] after execution.
     */
    fun undo()

    /**
     * Redoes the first transaction in the redo stack by calling the [Command.execute] method of all its [Command].
     *
     * This method validates the [Drawing] on which the undone head [Command] operates.
     * Posts a [CommandEvent] after execution.
     */
    fun redo()

    /**
     * Resets the state of this [CommandManager] by deleting all undo and redo entries and posting a [CommandEvent].
     * Posts a [CommandEvent] after execution.
     */
    fun reset()
}