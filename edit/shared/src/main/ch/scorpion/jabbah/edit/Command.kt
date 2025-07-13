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
	 */
	fun undo()
}

/**
 * A [Command] encapsulates a change of a [Drawing]'s state in order to make the change undoable and
 * redoable.
 *
 * [Command] that want to provide their own (simple!) undo logic can implement the [Undoable] interface,
 * thereby avoiding the need to replay from snapshots when undoing this [Command].
 *
 * A [Command] can have tags set by the [CommandManager] to which it belongs.
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
	 * Returns a detailed description of that this [Command] does, including
	 * the ID of the changed object, and the values applied in the change.
	 * Used for logging and debugging.
	 * Can in general only be called AFTER [execute].
	 * Would have wanted to use [kotlin.reflect.KClass.qualifiedName], but this is not yet supported by Kotlin JS.
	 * [kotlin.reflect.KClass.simpleName] doesn't scale well with obfuscation.
	 */
	fun getDetailedDescription(): String = getDescription()

    /**
     * Executes the change of a [Drawing]'s state that is associated with this [Command].
     *
     * This method is called by the [CommandManager] to reset the effect of a previous undo action. But it can
     * also be called by the code that created this [Command], thus avoiding duplicate code for changing the state
     * of the [Drawing].
     */
    fun execute()

    /**
     * Called by the [CommandManager] in order to validate any [View] that displays the changed
     * [Drawing] after [execute] or [Undoable.undo] has been done.
     */
    fun validate()

	/** Sets a set of tags in this [Command].*/
	fun setTags(vararg names: String)

	/** Determines if tag [name] has previously been set using [setTags].*/
	fun hasTag(name: String): Boolean

	/**
	 * Called by [CommandManager] during an undo process if this [Command] does not implement [Undoable].
	 * This is only a notification giving non-[Undoable] [Command]s a chance to undo things before
	 * a [CommandManager] that uses snapshots replays a snapshot.
	 * This is a special and rare case, therefore the default implementation is empty.
	 */
	fun notifyUndo() {}
}