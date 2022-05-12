package ch.scorpion.jabbah.edit

import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.io.Storable

/** Posted by a [CommandManager] on its [EventBus] whenever a [Command] has been registered, done or undone.*/
data class CommandEvent(val commandManager: CommandManager)

/** Posted by a [CommandManager] on its [EventBus] whenever its 'active' state has changed.*/
data class CommandManagerActiveEvent(val commandManager: CommandManager)

interface UndoableDataHolder {

	fun getUndoableState(): Storable?

	fun setUndoableState(state: Storable)
}

/**
 * Manages a list of [Command]s that can be played back and forth to support undoability and redoablity of these
 * [Command]s.
 *
 * Posts a [CommandEvent] on a [CommandManager]'s [EventBus] whenever a [Command] has been
 * registered, executed or undone.
 *
 * A [CommandManager] resets its state after the current application data have been saved.
 *
 * [CommandManager] provides "nested transactions" which mainly represent combined [Command]s. When a transaction
 * is started, all subsequently executed or registered [Command]s are stored as children of the the first [Command]
 * until the transaction is either committed or rolled back. Such [Command]s count in terms of "undo/redo" only as
 * a single [Command]. This can be used for complex application logic, where a single user action can result in
 * multiple [Command]s. If no transaction has been began explicitly, execution or registration starts an
 * implicit transaction, which gets immediately auto-committed. During a transaction, undo/redo is not possible,
 * i.e. transaction can only be used while processing a single user action.
 *
 * [CommandManager] uses the concept of "checkpoints" for stacking [CommandManager] states. Consider an
 * application that uses a single [CommandManager] or an [Editor]. This application consists of a modal dialog,
 * in which the user performs some undoable actions. If the user closes this dialog using "Cancel" (and confirming
 * a warning that he will loose his changes), the application want the [CommandManager] to delete all registered
 * [Command]s back to the point where the modal dialog was opened. This can be done by opening a checkpoint when
 * the dialog is opened, and closing the checkpoint when the dialog is closed. It is up to the client code to
 * execute or register a special [Command] that represents the changes performed since opening the checkpoint,
 * or to resign to do so if these changes should be abandoned.
 *
 * [CommandManager] supports a tagging system. Systems that use a [CommandManager] can set a tag in this [CommandManager].
 * Every [Command] that is subsequently added will automatically received that tag as well. This allows systems to
 * determine whether [Command]s from a particular subsystem have been added since the last storing operation.
 * Use [addTag] to add a tag and [removeTag] to remove it, and [hasCommandWithTag] to check if a [Command]
 * with a particular tags exists.
 */
interface CommandManager {

	/**
	 * Determines whether this [CommandManager] is active.
	 * A [CommandManager] can be set 'inactive' by other systems that create [Command]s, such as [Editor]s.
	 * This property is primarily used for disabling undo and redo [Action]s.
	 */
	var active: Boolean

    /** The [EventBus] used by this [CommandManager].*/
    val eventBus: EventBus

	val isInTransaction: Boolean

	/** The number of [Command] that could be undone, and therefore the number of unsaved changes.*/
	val commandCount: Int

	fun bindDataHolder(dataHolder: UndoableDataHolder)

    /**
     * Begins a new transaction with the specified [Command] as its head, and executes or registers the [Command]
     * depending on the [register] property.
     * If there is already a current transaction, the specified [Command] is added to that transaction, and the nesting
     * level is increased. That nesting level must be met by a corresponding number of commits.
     *
     * @param register `true` if [command] should only be registered and not executed. If omitted,
     * the [Command] is executed
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
     * If there is no current transaction, this [CommandManager] begins a transaction for the specified [Command]
     * and commits it automatically after executing the [Command].
     */
    fun execute(command: Command)

    /**
     * Registers the specified [Command] with the current transaction without executing it.
     * If there is no current transaction, this [CommandManager] begins a transaction for the specified [Command]
     * and commits it automatically after registering the [Command].
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
     * Undoes the last transaction in the undo stack by calling the [Undoable.undo] method of all its [Command]
     * in reverse order, if possible.
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

	/**
	 * Creates a new, stacked [CommandManager] state. See the class comment for more information.
	 * @name the name of the checkpoint. Only used for logging.
	 */
	fun openCheckpoint(name: String)

	/**
	 * Closes the previously opened checkpoint.
	 * @throws IllegalStateException if no checkpoint has been opened
	 */
	fun closeCheckpoint()

	/**
	 * Adds a tag with the specified name. This tag will be attached to all subsequently added [Command]s.
	 */
	fun addTag(name: String)

	/**
	 * Removes the tag with the specified name. This tag will not be attached anymore to all subsequently added [Command]s.
	 */
	fun removeTag(name: String)

	/**
	 * Checks if there is any [Command] with the specified tag name.
	 */
	fun hasCommandWithTag(name: String): Boolean
}