package io.antarescircuit.antares.web

import io.antarescircuit.antares.model.addressable.AbstractAddressable
import io.antarescircuit.antares.model.addressable.Addressable
import io.antarescircuit.antares.model.addressable.LogisimMemoryDump
import io.antarescircuit.antares.model.addressable.Memory
import io.antarescircuit.antares.model.addressable.MemoryDump
import io.antarescircuit.antares.model.addressable.MemoryStorable
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.Undoable
import io.antarescircuit.jabbah.edit.command.AbstractCommand
import io.antarescircuit.jabbah.graph.model.vertice.ObjectLink
import io.antarescircuit.jabbah.graph.view.GraphElementView
import io.antarescircuit.jabbah.graph.view.GraphView

/**
 * Loads the contents of an [Addressable] from a memory dump, like the desktop's `AddressableContentsCommand`,
 * which reads the dump from a file. [contents] is parsed and validated before the command is executed.
 */
internal class WebMemoryContentsCommand(
	private val drawingView: DrawingView<GraphElementView<*>, GraphView>?,
	private val link: ObjectLink<Addressable>,
	contents: String,
	private val source: String,
) : AbstractCommand("antares.command.memoryContents"), Undoable {

	private val addressable: Addressable get() = link.getLinkedObject(drawingView?.drawing?.graph)

	private val newContents: Memory = parse(addressable, contents)
	private var oldContents: Memory? = null
	private var oldDataSource: String? = null

	override fun execute() {
		oldContents = addressable.memory.copy()
		oldDataSource = addressable.dataSource
		addressable.memory.replaceWith(newContents)
		addressable.dataSource = source
		addressable.update()
	}

	override fun undo() {
		addressable.memory.replaceWith(oldContents!!)
		addressable.dataSource = oldDataSource
		addressable.update()
	}

	companion object {
		/** Parses an Antares or Logisim dump, validating it against the widths of [addressable]. */
		fun parse(addressable: Addressable, contents: String): Memory {
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
	}
}

/** Changes the address or data width of the open [MemoryStorable] (desktop: `MemoryStorableBeanInfo`). */
internal class WebMemoryWidthCommand(
	private val storable: () -> MemoryStorable,
	private val isAddress: Boolean,
	private val newValue: BitWidth,
) : AbstractCommand(if (isAddress) "element.property.addressBitWidth.name" else "element.property.dataBitWidth.name"), Undoable {

	private var oldValue: BitWidth? = null

	override fun execute() {
		val s = storable()
		oldValue = if (isAddress) s.addressWidth else s.dataWidth
		set(newValue)
	}

	override fun undo() {
		set(oldValue!!)
	}

	private fun set(value: BitWidth) {
		if (isAddress) storable().addressWidth = value else storable().dataWidth = value
	}
}
