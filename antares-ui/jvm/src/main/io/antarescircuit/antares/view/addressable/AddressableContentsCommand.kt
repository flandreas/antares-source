package io.antarescircuit.antares.view.addressable

import io.antarescircuit.antares.model.addressable.Addressable
import io.antarescircuit.antares.model.addressable.MemoryDump
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.edit.Command
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.Undoable
import io.antarescircuit.jabbah.edit.command.AbstractCommand
import io.antarescircuit.jabbah.graph.model.vertice.ObjectLink
import io.antarescircuit.jabbah.graph.view.GraphElementView
import io.antarescircuit.jabbah.graph.view.GraphView
import java.nio.file.Files
import java.nio.file.Paths

/**
 * A [Command] for loading the contents of an [Addressable] from a file.
 */
class AddressableContentsCommand(
	private val drawingView: DrawingView<GraphElementView<*>, GraphView>?,
	private val link: ObjectLink<Addressable>,
	private val bitWidth: BitWidth,
	private val filePath: String
) : AbstractCommand("antares.command.memoryContents"), Undoable {

	companion object {
		private val LOG by logger(AddressableContentsCommand::class)
	}

	private val addressable: Addressable get() = link.getLinkedObject(drawingView?.drawing?.graph)

	private var oldContents: String? = null

	private var oldDataSource: String? = addressable.dataSource

	override fun execute() {
		oldContents = MemoryDump.write(addressable.memory, bitWidth)
		try {
			MemoryDump.read(addressable.memory, String(Files.readAllBytes(Paths.get(filePath))))
			addressable.validateDataBitWidth(addressable.dataWidth)
			addressable.dataSource = filePath
			addressable.update()
		} catch (e: Throwable) {
			LOG.debug("Error while reading memory from file '$filePath'")
			throw e
		}
	}

	override fun undo() {
		MemoryDump.read(addressable.memory, oldContents!!)
		addressable.dataSource = oldDataSource
		addressable.update()
	}
}