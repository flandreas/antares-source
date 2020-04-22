package ch.scorpion.jabbah.edit.command

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
 * Instead of relying on "undo" logic in [Command], [SourcingCommandManager] stores snapshots of the
 * [Storable] application data. When a [Command] has to be undone, [SourcingCommandManager] takes the most recent
 * snapshot and replays all [Command]s registered since then, except the one to be undone.
 *
 * When the application resets it undoable state, it is required to communicate this to this [SourcingCommandManager]
 * by calling [SourcingCommandManager.reset], which will then clean its undo and redo stacks and create a new
 * snapshot.
 *
 * Note that is is not sufficient to create new snapshots in [execute], because clients can change the application
 * state during complex editing operations, and [register] a corresponding [Command] afterwards.
 */
class SourcingCommandManager(
	private val maxCommandCountPerSnapshot: Int = DEF_MAX_COMMAND_COUNT_PER_SNAPSHOT,
	override val eventBus: EventBus = BaseModule.eventBus
) : CommandManager {

	companion object {
		private val LOG by logger(SourcingCommandManager::class)
		private const val DEF_MAX_COMMAND_COUNT_PER_SNAPSHOT = 10
	}

	private val snapshots = Stack<Snapshot>()
	private val redoSnapshots = Stack<Snapshot>()
	private var transaction: Transaction? = null
	private var transactionLevel: Int = 0

	private lateinit var undoableDataHolder: UndoableDataHolder

	val snapshotCount: Int get() = snapshots.size
	val redoSnapshotCount: Int get() = redoSnapshots.size

	/** ---- [CommandManager] interface */

	override var active: Boolean = true
		set(value) {
			if (value != field) {
				field = value
				eventBus.post(CommandManagerActiveEvent(this))
			}
		}

	override fun bindDataHolder(dataHolder: UndoableDataHolder) {
		LOG.debug("Binding to $dataHolder")
		undoableDataHolder = dataHolder
		reset()
	}

	override fun canUndo(): Boolean {
		return transactionLevel == 0 && !snapshots.empty && snapshots.peek().canUndo
	}

	override fun canRedo(): Boolean {
		return transactionLevel == 0 && !snapshots.empty && snapshots.peek().canRedo || !redoSnapshots.empty
	}

	override fun reset() {
		LOG.debug("reset, creating snapshot")
		snapshots.clear()
		redoSnapshots.clear()
		undoableDataHolder.getUndoableState()?.let { addableSnapshot() }
		eventBus.post(CommandEvent(this))
	}

	override fun register(command: Command) {
		LOG.debug("Register command '${command.getDescription()}'")
		if (transaction == null) {
			beginTransaction(command, register = true)
			commitTransaction()
		} else {
			transaction!!.add(command)
			command.validate()
		}
	}

	override fun execute(command: Command) {
		LOG.debug("Execute command '${command.getDescription()}'")
		if (transaction == null) {
			beginTransaction(command, register = false)
			commitTransaction()
		} else {
			transaction!!.add(command)
			command.execute()
			command.validate()
		}
	}
	override fun undo() {
		if (!canUndo()) {
			throw IllegalStateException("no undoable command")
		}
		val snapshot = snapshots.peek()
		LOG.debug("Undo command '${snapshot.undoDescription}'")
		snapshot.undo()

		transferUndoneSnapshotIfNecessary(storeForRedo = true)

		eventBus.post(CommandEvent(this))
	}

	private fun transferUndoneSnapshotIfNecessary(storeForRedo: Boolean) {
		val snapshot = snapshots.peek()
		if (snapshot.undoStack.empty && snapshots.size > 1) {
			snapshots.pop()
			if (storeForRedo) {
				redoSnapshots.push(snapshot)
			}
		}

		if (snapshots.empty) {
			addSnapshot()
		}
	}

	override fun redo() {
		if (!canRedo()) {
			throw IllegalStateException("no redoable command")
		}

		if (!snapshots.peek().canRedo) {
			snapshots.push(redoSnapshots.pop())
		}

		LOG.debug("Redo command '${snapshots.peek().redoDescription}'")
		snapshots.peek().redo()

		eventBus.post(CommandEvent(this))
	}

	override fun beginTransaction(command: Command, register: Boolean) {
		if (transaction == null) {
			transaction = Transaction()
			addableSnapshot().add(transaction!!)
		}
		transactionLevel++
		transaction?.let {
			it.add(command)
			if (register) {
				command.registered()
			} else {
				command.execute()
				command.validate()
			}
		}
	}

	override fun beginTransaction(descriptionKey: String, drawingView: DrawingView<*>?) {
		beginTransaction(TransactionCommand(descriptionKey, drawingView), register = true)
	}

	override fun commitTransaction() {
		if (transaction == null) {
			throw IllegalStateException("no transaction to commit")
		}
		transactionLevel--
		if (transactionLevel == 0) {
			LOG.debug("Commit transaction")
			eventBus.post(CommandEvent(this))
			transaction = null
		}
	}

	override fun rollbackTransaction() {
		if (transaction == null) {
			throw IllegalStateException("no transaction to rollback")
		}
		snapshots.peek().undo()
		transferUndoneSnapshotIfNecessary(storeForRedo = false)

		transactionLevel = 0
		transaction = null
	}

	override fun getUndoDescription(): String? {
		if (!canUndo()) {
			return null
		}
		return snapshots.peek().undoDescription
	}

	override fun getRedoDescription(): String? {
		if (!canRedo()) {
			return null
		}
		return snapshots.peek().redoDescription
	}

	override fun openCheckpoint(name: String) {
		throw UnsupportedOperationException("not implemented")
	}

	override fun commitCheckpoint() {
		throw UnsupportedOperationException("not implemented")
	}

	override fun rollbackCheckpoint() {
		throw UnsupportedOperationException("not implemented")
	}

	override fun closeCheckpoint() {
		throw UnsupportedOperationException("not implemented")
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
				(it as Undoable).undo1()
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

		override fun undo1() {
			// empty
		}

		override fun validate() {
			drawingView?.drawing?.validate()
		}
	}

	/**
	 * Checks if the current [Snapshot] is already too big and creates a new [Snapshot] if so. Returns in any case
	 * the [Snapshot] where a new [Command] can be added.
	 */
	private fun addableSnapshot(): Snapshot {
		if (snapshots.empty || snapshots.peek().undoCommandCount >= maxCommandCountPerSnapshot) {
			addSnapshot()
		}
		return snapshots.peek()
	}

	private fun addSnapshot() {
		snapshots.push(Snapshot(createSnapshotData()))
	}

	private fun createSnapshotData(): Storable {
		LOG.debug("Create new snapshot")
		return StorableCloner.clone(undoableDataHolder.getUndoableState()!!)
	}
}