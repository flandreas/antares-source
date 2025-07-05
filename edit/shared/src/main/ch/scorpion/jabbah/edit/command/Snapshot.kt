package ch.scorpion.jabbah.edit.command

import ch.scorpion.jabbah.base.Disposable
import ch.scorpion.jabbah.base.collection.Stack
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.edit.UndoableDataHolder
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StorableCloner

/**
 * Part of the state of [SourcingCommandManager].
 * Consists of a data snapshot, on which [Transaction]s are added to represent undoable
 * changes of that data.
 */
internal class Snapshot(
	private val data: Storable,
	private var undoableDataHolder: UndoableDataHolder
) : Disposable {

	companion object {
		private val LOG by logger(Snapshot::class)
	}

	val undoStack = Stack<Transaction>()
	private val redoStack = Stack<Transaction>()

	val undoCommandCount: Int get() = undoStack.size
	val undoDescription: String get() = undoStack.optionalPeek()?.headCommand?.getDescription() ?: ""
	val redoDescription: String get() = redoStack.optionalPeek()?.headCommand?.getDescription() ?: ""
	val canUndo: Boolean get() = !undoStack.empty
	val canRedo: Boolean get() = !redoStack.empty

	override fun dispose() {
		if (data is Disposable) {
			data.dispose()
		}
	}

	fun add(transaction: Transaction) {
		undoStack.push(transaction)
	}

	fun undo(forRedo: Boolean) {
		val transaction = undoStack.pop()
		transaction.notifyUndo()
		if (forRedo) {
			redoStack.push(transaction)
		}

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

	fun hasTag(name: String): Boolean =
		undoStack.items.any { it.hasTag(name) }

	private fun replayFromSnapshot() {
		// Calculate before executing the Commands, as their properties may become invalid
		// upon execution of the next Commands
		val undoStackWriter = TransactionStackWriter("Undo stack")

		try {
			val clonedData = StorableCloner.clone(data)
			LOG.trace("Clone snapshot and set as new undoable data $clonedData")

			undoableDataHolder.setUndoableState(clonedData)

			LOG.trace("Replaying")
			undoStack.items.forEach {
				LOG.trace(".. replaying transaction ${it.headCommand.getDescription()}")
				it.execute()
				undoStackWriter.write(it)
			}

			undoableDataHolder.undoableStateEstablished(clonedData)
		} catch (e: Throwable) {
			LOG.error("Error in replaying snapshot for undo", e)
			LOG.error(undoStackWriter.toString())
			// Add to user trail so this gets uploaded and notified to developers
			LOG.userTrail(undoStackWriter.toString())
			throw e
		}
	}
}