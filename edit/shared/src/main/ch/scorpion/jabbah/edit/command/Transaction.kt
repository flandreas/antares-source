package ch.scorpion.jabbah.edit.command

import ch.scorpion.jabbah.edit.Command
import ch.scorpion.jabbah.edit.Undoable

/** A [Transaction] in a [SourcingCommandManager] contains multiple [Command]s. */
internal class Transaction {
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
		check(canUndo) { "Cannot undo Transaction" }
		commands.reversed().forEach {
			(it as Undoable).undo()
			it.validate()
		}
	}

	fun hasTag(name: String): Boolean =
		commands.any { it.hasTag(name) }
}