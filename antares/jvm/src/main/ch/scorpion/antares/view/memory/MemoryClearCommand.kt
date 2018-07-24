package ch.scorpion.antares.view.memory

import ch.scorpion.antares.model.memory.Memory
import ch.scorpion.antares.model.memory.MemoryDump
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.jabbah.edit.Command
import ch.scorpion.jabbah.edit.command.AbstractCommand

/** A [Command] for clearing the contents of a [Memory].*/
class MemoryClearCommand(
	private val memory: Memory,
	private val bitWidth: BitWidth
) : AbstractCommand("antares.command.clearMemory") {

	private var oldContents: String? = null

	override fun execute() {
		oldContents = MemoryDump.write(memory, bitWidth)
		memory.clear()
	}

	override fun undo() {
		MemoryDump.read(memory, oldContents!!)
	}
}