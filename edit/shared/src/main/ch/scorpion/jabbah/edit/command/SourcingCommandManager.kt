package ch.scorpion.jabbah.edit.command

import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.checkState
import ch.scorpion.jabbah.base.collection.Stack
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.exception.IllegalStateException
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StorableCloner

/**
 * A [CommandManager] implementation that uses "command sourcing".
 *
 * Simple [Command]s implement [Undoable], which allows this [SourcingCommandManager] to simpy call
 * [Undoable.undo] when the [Command] has to be undone.
 *
 * In addition to relying on "undo" logic in [Command], [SourcingCommandManager] stores snapshots of the
 * [Storable] application data. When a [Command] has to be undone that doesn't implement [Undoable],
 * [SourcingCommandManager] takes the most recent [Snapshot] and replays all [Command]s registered since then,
 * except the one to be undone.
 *
 * When the application resets it undoable state, it is required to communicate this to this [SourcingCommandManager]
 * by calling [SourcingCommandManager.reset], which will then clean its undo and redo stacks and create a new
 * snapshot.
 *
 * When this [SourcingCommandManager] creates a new [Snapshot], it is set on [UndoableDataHolder]
 * as the new current undoable state.
 *
 * Note that is is not sufficient to create new [Snapshot]s in [execute], because clients can change the application
 * state during complex editing operations, and [register] a corresponding [Command] afterwards.
 */
class SourcingCommandManager(
	override val eventBus: EventBus = BaseModule.eventBus,
	private val properties: Properties = BaseModule.properties
) : CommandManager {

	companion object {
		private val LOG by logger(SourcingCommandManager::class)
		const val PROP_MAX_COMMAND_COUNT_PER_SNAPSHOT = "edit.commandManager.cmdPerSnapshot"
		private const val DEFAULT_STATE_NAME = "default"
	}

	/** Used to implement checkpoints.*/
	private class State(val name: String) {
		val snapshots = Stack<Snapshot>()
		val redoSnapshots = Stack<Snapshot>()
		var transaction: Transaction? = null
		var transactionLevel: Int = 0

		val snapshotCount: Int get() = snapshots.size
		val redoSnapshotCount: Int get() = redoSnapshots.size
	}

	private val maxCommandCountPerSnapshot: Int get() = properties.getInt(PROP_MAX_COMMAND_COUNT_PER_SNAPSHOT)

	private lateinit var undoableDataHolder: UndoableDataHolder

	private val states = Stack<State>()
	private val state: State get() = states.peek()

	init {
		states.push(State(DEFAULT_STATE_NAME))
	}

	val snapshotCount: Int get() = state.snapshotCount
	val redoSnapshotCount: Int get() = state.redoSnapshotCount

	/** ---- [CommandManager] interface */

	override var active: Boolean = true
		set(value) {
			if (value != field) {
				field = value
				eventBus.post(CommandManagerActiveEvent(this))
			}
		}

	override val isInTransaction: Boolean get() = state.transaction != null

	override fun bindDataHolder(dataHolder: UndoableDataHolder) {
		LOG.debug("Binding to $dataHolder")
		undoableDataHolder = dataHolder
		reset()
	}

	override fun canUndo(): Boolean {
		return state.transactionLevel == 0 && !state.snapshots.empty && state.snapshots.peek().canUndo
	}

	override fun canRedo(): Boolean {
		return state.transactionLevel == 0 && !state.snapshots.empty && state.snapshots.peek().canRedo || !state.redoSnapshots.empty
	}

	override fun reset() {
		LOG.debug("reset, creating snapshot")
		resetUndo()
		resetRedo()
		undoableDataHolder.getUndoableState()?.let { addableSnapshot() }
		eventBus.post(CommandEvent(this))
	}

	override fun register(command: Command) {
		LOG.debug("Register command '${command.getDescription()}'")
		resetRedo()
		if (state.transaction == null) {
			beginTransaction(command, register = true)
			commitTransaction()
		} else {
			state.transaction!!.add(command)
			command.validate()
		}
	}

	override fun execute(command: Command) {
		LOG.debug("Execute command '${command.getDescription()}'")
		resetRedo()

		if (state.transaction == null) {
			beginTransaction(command, register = false)
			commitTransaction()
		} else {
			state.transaction!!.add(command)
			try {
				command.execute()
			} catch (e: Exception) {
				LOG.error("Exception in CommandManager.execute", e)
				rollbackTransaction()
				throw e
			}
			command.validate()
		}
	}
	override fun undo() {
		if (!canUndo()) {
			throw IllegalStateException("no undoable command")
		}
		val snapshot = state.snapshots.peek()
		LOG.debug("Undo command '${snapshot.undoDescription}'")
		snapshot.undo()

		transferUndoneSnapshotIfNecessary(storeForRedo = true)

		eventBus.post(CommandEvent(this))
	}

	private fun transferUndoneSnapshotIfNecessary(storeForRedo: Boolean) {
		val snapshot = state.snapshots.peek()
		if (snapshot.undoStack.empty && state.snapshots.size > 1) {
			state.snapshots.pop()
			if (storeForRedo) {
				state.redoSnapshots.push(snapshot)
			}
		}

		if (state.snapshots.empty) {
			addSnapshot()
		}
	}

	override fun redo() {
		if (!canRedo()) {
			throw IllegalStateException("no redoable command")
		}

		if (!state.snapshots.peek().canRedo) {
			state.snapshots.push(state.redoSnapshots.pop())
		}

		LOG.debug("Redo command '${state.snapshots.peek().redoDescription}'")
		state.snapshots.peek().redo()

		eventBus.post(CommandEvent(this))
	}

	override fun beginTransaction(command: Command, register: Boolean) {
		if (state.transaction == null) {
			state.transaction = Transaction()
			addableSnapshot().add(state.transaction!!)
		}
		state.transactionLevel++
		state.transaction?.let {
			it.add(command)
			if (!register) {
				try {
					command.execute()
				} catch (e: Throwable) {
					rollbackTransaction()
					throw  e
				}
				command.validate()
			}
		}
	}

	override fun beginTransaction(descriptionKey: String, drawingView: DrawingView<*>?) {
		beginTransaction(TransactionCommand(descriptionKey, drawingView), register = true)
	}

	override fun commitTransaction() {
		if (state.transaction == null) {
			throw IllegalStateException("no transaction to commit")
		}
		state.transactionLevel--
		if (state.transactionLevel == 0) {
			LOG.debug("Commit transaction")
			resetRedo()
			eventBus.post(CommandEvent(this))
			state.transaction = null
		}
	}

	override fun rollbackTransaction() {
		if (state.transaction == null) {
			throw IllegalStateException("no transaction to rollback")
		}
		state.snapshots.peek().undo()
		transferUndoneSnapshotIfNecessary(storeForRedo = false)

		state.transactionLevel = 0
		state.transaction = null
	}

	override fun getUndoDescription(): String? {
		if (!canUndo()) {
			return null
		}
		return state.snapshots.peek().undoDescription
	}

	override fun getRedoDescription(): String? {
		if (!canRedo()) {
			return null
		}
		return state.snapshots.peek().redoDescription
	}

	override fun openCheckpoint(name: String) {
		LOG.debug("Open checkpoint '$name'")
		states.push(State(name))
		eventBus.post(CommandEvent(this))
	}

	override fun closeCheckpoint() {
		if (states.size < 2) {
			throw IllegalStateException("no checkpoint to close")
		}
		LOG.debug("Close checkpoint ${states.peek().name}")
		states.pop()
		eventBus.post(CommandEvent(this))
	}

	/** ---- [SourcingCommandManager] */

	private inner class Snapshot(private val data: Storable) {
		val undoStack = Stack<Transaction>()
		val redoStack = Stack<Transaction>()

		val undoCommandCount: Int get() = undoStack.size
		val undoDescription: String? get() = undoStack.optionalPeek()?.headCommand?.getDescription() ?: ""
		val redoDescription: String? get() = redoStack.optionalPeek()?.headCommand?.getDescription() ?: ""
		val canUndo: Boolean get() = !undoStack.empty
		val canRedo: Boolean get() = !redoStack.empty

		fun add(transaction: Transaction) {
			undoStack.push(transaction)
		}

		fun undo() {
			val transaction = undoStack.pop()
			redoStack.push(transaction)

			if (transaction.canUndo) {
				transaction.undo()
			} else {
				replayFromSnapshot()
			}
		}

		fun redo() {
			val command = redoStack.pop()
			undoStack.push(command)
			command.execute()
		}

		fun resetRedo() {
			redoStack.clear()
		}

		private fun replayFromSnapshot() {
			val clonedData = StorableCloner.clone(data)
			LOG.debug("Clone snapshot and set as new undoable data $clonedData")
			undoableDataHolder.setUndoableState(clonedData)

			LOG.debug("Replaying")
			undoStack.items.forEach {
				LOG.debug(".. replaying transaction ${it.headCommand.getDescription()}")
				it.execute()
			}
		}
	}

	private class Transaction {
		private val commands = mutableListOf<Command>()
		val headCommand: Command get() = commands.first()

		val canUndo: Boolean get() = commands.all { it is Undoable && it.canUndo }

		fun add(command: Command) {
			commands.add(command)
		}

		fun execute() {
			for (c in commands) {
				c.execute()
				c.validate()
			}
		}

		fun undo() {
			checkState(canUndo, "Cannot undo Transaction")
			commands.reversed().forEach {
				(it as Undoable).undo()
				it.validate()
			}
		}
	}

	/**
	 * A [Command] implementation that does nothing, but serves only as a dummy [Command]
	 * for holding inner transaction [Command]s.
	 *
	 * @param descriptionKey the translation key of the transaction's description
	 * @property drawingView the [DrawingView] to validate, if any
	 */
	private class TransactionCommand(
		descriptionKey: String,
		private val drawingView: DrawingView<*>? = null
	) : AbstractCommand(descriptionKey, null), Undoable {

		override fun execute() {
			// empty
		}

		override fun undo() {
			// empty
		}

		override fun validate() {
			drawingView?.drawing?.validate()
		}
	}

	private fun resetUndo() {
		state.snapshots.clear()
	}

	private fun resetRedo() {
		if (!state.snapshots.empty) {
			state.snapshots.peek().resetRedo()
		}
		state.redoSnapshots.clear()
	}

	/**
	 * Checks if the current [Snapshot] is already too big and creates a new [Snapshot] if so. Returns in any case
	 * the [Snapshot] where a new [Command] can be added.
	 */
	private fun addableSnapshot(): Snapshot {
		if (state.snapshots.empty || state.snapshots.peek().undoCommandCount >= maxCommandCountPerSnapshot) {
			addSnapshot()
		}
		return state.snapshots.peek()
	}

	private fun addSnapshot() {
		state.snapshots.push(Snapshot(createSnapshotData()))
	}

	private fun createSnapshotData(): Storable {
		LOG.debug("Create new snapshot")
		return StorableCloner.clone(undoableDataHolder.getUndoableState()!!)
	}
}