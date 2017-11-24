package ch.scorpion.antares.view.memory

import ch.scorpion.antares.model.memory.Memory
import ch.scorpion.antares.model.memory.MemoryDump
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.jabbah.edit.command.AbstractCommand
import ch.scorpion.jabbah.base.logger
import java.nio.file.Files
import java.nio.file.Paths

/**
 * A [Command] for loading the contents of a [Memory] from a file.
 */
class MemoryContentsCommand(
        private val memory: Memory,
        private val bitWidth: BitWidth,
        private val filePath: String
) : AbstractCommand("antares.command.memoryContents", null) {

    companion object {
        private val LOG by logger(MemoryContentsCommand::class)
    }

    private var oldContents: String? = null

    override fun execute() {
        oldContents = MemoryDump.write(memory, bitWidth)
        try {
            MemoryDump.read(memory, String(Files.readAllBytes(Paths.get(filePath))))
        } catch(e: Throwable) {
            LOG.error("Error while reading memory from file '$filePath'")
            throw e
        }
    }

    override fun undo() {
        MemoryDump.read(memory, oldContents!!)
    }
}