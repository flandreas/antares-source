package ch.scorpion.antares.view.memory

import ch.scorpion.antares.model.memory.Memory
import ch.scorpion.jabbah.edit.command.AbstractCommand

/** Represents the change of the value of a memory cell by the user.*/
data class MemoryCellChange(val address: Int, val newValue: Long, val origValue: Long)

class MemoryCellChangeCommand(
	private val memory: Memory,
	private val changes: Collection<MemoryCellChange>
) : AbstractCommand("antares.command.memoryContents", null) {

	override fun execute() {
		changes.forEach { memory.write(it.address, it.newValue) }
	}

	override fun undo() {
		changes.forEach { memory.write(it.address, it.origValue) }
	}
}