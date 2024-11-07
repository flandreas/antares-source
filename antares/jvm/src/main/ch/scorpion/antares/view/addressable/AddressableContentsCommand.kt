package ch.scorpion.antares.view.addressable

import ch.scorpion.antares.model.addressable.Addressable
import ch.scorpion.antares.model.addressable.MemoryDump
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.edit.Command
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.Undoable
import ch.scorpion.jabbah.graph.app.AbstractGraphViewCommand
import ch.scorpion.jabbah.graph.model.vertice.VerticeLink
import ch.scorpion.jabbah.graph.view.GraphView
import java.nio.file.Files
import java.nio.file.Paths

/**
 * A [Command] for loading the contents of an [Addressable] from a file.
 */
class AddressableContentsCommand(
	view: DrawingView<GraphView>,
	private val link: VerticeLink,
	private val bitWidth: BitWidth,
	private val filePath: String
) : AbstractGraphViewCommand("antares.command.memoryContents", view), Undoable {

	companion object {
		private val LOG by logger(AddressableContentsCommand::class)
	}

	private val addressable: Addressable get() = link.getLinkedVertice(drawingView.drawing.graph!!) as Addressable

	private var oldContents: String? = null

	private var oldDataSource: String? = addressable.dataSource

	override fun execute() {
		oldContents = MemoryDump.write(addressable.memory, bitWidth)
		try {
			MemoryDump.read(addressable.memory, String(Files.readAllBytes(Paths.get(filePath))))
			addressable.dataSource = filePath
			addressable.update()
		} catch (e: Throwable) {
			LOG.error("Error while reading memory from file '$filePath'")
			throw e
		}
	}

	override fun undo() {
		MemoryDump.read(addressable.memory, oldContents!!)
		addressable.dataSource = oldDataSource
		addressable.update()
	}
}