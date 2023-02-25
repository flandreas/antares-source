package ch.scorpion.jabbah.edit.command

import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.edit.Command
import ch.scorpion.jabbah.edit.Undoable

/** A [Transaction] in a [SourcingCommandManager] contains multiple [Command]s. */
internal class Transaction {

	companion object {
		private val LOG by logger(Transaction::class)
	}

	val commands = mutableListOf<Command>()
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
		check(canUndo) { "Cannot undo Transaction" }
		try {
			commands.reversed().forEach {
				(it as Undoable).undo()
				it.validate()
			}
		} catch (e: Throwable) {
			LOG.error("Error in undoing Transaction", e)
			throw e
		}
	}

	fun hasTag(name: String): Boolean =
		commands.any { it.hasTag(name) }

	fun notifyUndo() {
		commands.filter { it !is Undoable }.reversed().forEach { it.notifyUndo() }
	}
}