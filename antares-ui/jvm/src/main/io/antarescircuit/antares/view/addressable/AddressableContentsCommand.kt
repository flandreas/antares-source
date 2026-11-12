package io.antarescircuit.antares.view.addressable

import io.antarescircuit.antares.model.addressable.Addressable
import io.antarescircuit.antares.model.addressable.MemoryDump
import io.antarescircuit.antares.model.addressable.Memory
import io.antarescircuit.antares.model.addressable.LogisimMemoryDump
import io.antarescircuit.antares.model.addressable.AbstractAddressable
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
	private val filePath: String
) : AbstractCommand("antares.command.memoryContents"), Undoable {

	companion object {
		private val LOG by logger(AddressableContentsCommand::class)
	}

	private val addressable: Addressable get() = link.getLinkedObject(drawingView?.drawing?.graph)

	private var oldContents: Memory? = null

	private var newContents: Memory? = null

	private var oldDataSource: String? = addressable.dataSource

	override fun execute() {
		try {
			oldContents = addressable.memory.copy()
			oldDataSource = addressable.dataSource
			val parsed = newContents ?: readValidated().also { newContents = it }
			addressable.memory.replaceWith(parsed)
			addressable.dataSource = filePath
			addressable.update()
		} catch (e: Throwable) {
			LOG.debug("Error while reading memory from file '$filePath'")
			throw e
		}
	}

	private fun readValidated(): Memory {
		val contents = Files.readString(Paths.get(filePath))
		val parsed = Memory()
		if (LogisimMemoryDump.recognizes(contents)) {
			LogisimMemoryDump.read(parsed, contents, addressable.maxAddress, addressable.dataWidth.maxValue)
		} else {
			MemoryDump.read(parsed, contents)
			AbstractAddressable.validateDataBitWidth(parsed, addressable.dataWidth)
			val cells = parsed.getNonZeroCells()
			while (cells.hasNext()) {
				require(cells.next().address <= addressable.maxAddress) { "Memory image exceeds address capacity" }
			}
		}
		return parsed
	}

	override fun undo() {
		addressable.memory.replaceWith(oldContents!!)
		addressable.dataSource = oldDataSource
		addressable.update()
	}
}