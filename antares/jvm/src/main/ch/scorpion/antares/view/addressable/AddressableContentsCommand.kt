package ch.scorpion.antares.view.addressable

import ch.scorpion.antares.model.addressable.Addressable
import ch.scorpion.antares.model.addressable.MemoryDump
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.edit.Command
import ch.scorpion.jabbah.edit.Undoable
import ch.scorpion.jabbah.edit.command.AbstractCommand
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.view.GraphView
import java.nio.file.Files
import java.nio.file.Paths

/**
 * A [Command] for loading the contents of an [Addressable] from a file.
 */
class AddressableContentsCommand(
	private val application: Application,
	private val addressableId: Int,
	private val bitWidth: BitWidth,
	private val filePath: String
) : AbstractCommand("antares.command.memoryContents", null), Undoable {

	companion object {
		private val LOG by logger(AddressableContentsCommand::class)
	}

	private val graphView: GraphView get() = (application.data!!.content as MetaGraph).graph.graphView
	private val addressable: Addressable get() = graphView.graph!!.withId(addressableId) as Addressable

	private var oldContents: String? = null

	override fun execute() {
		oldContents = MemoryDump.write(addressable.memory, bitWidth)
		try {
			MemoryDump.read(addressable.memory, String(Files.readAllBytes(Paths.get(filePath))))
		} catch (e: Throwable) {
			LOG.error("Error while reading memory from file '$filePath'")
			throw e
		}
	}

	override fun undo() {
		MemoryDump.read(addressable.memory, oldContents!!)
	}
}