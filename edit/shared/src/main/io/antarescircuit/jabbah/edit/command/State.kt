package io.antarescircuit.jabbah.edit.command

import io.antarescircuit.jabbah.base.collection.Stack
import io.antarescircuit.jabbah.edit.UndoableDataHolder

/** Used to implement checkpoints in [SourcingCommandManager].*/
internal class State(val name: String, val dataHolder: UndoableDataHolder) {

	val snapshots = Stack<Snapshot>()
	val redoSnapshots = Stack<Snapshot>()
	var transaction: Transaction? = null
	var transactionLevel: Int = 0

	val snapshotCount: Int get() = snapshots.size
	val redoSnapshotCount: Int get() = redoSnapshots.size

	val commandCount: Int get() = snapshots.items.sumOf { it.undoCommandCount }

	val tags: MutableSet<String> = mutableSetOf()

	fun hasCommandWithTag(name: String): Boolean =
		snapshots.items.any { it.hasTag(name) }

	fun dispose() {
		snapshots.dispose()
		redoSnapshots.dispose()
	}
}