package io.antarescircuit.antares.model.addressable

import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.jabbah.edit.Command
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.Undoable
import io.antarescircuit.jabbah.edit.command.AbstractCommand
import io.antarescircuit.jabbah.graph.model.vertice.ObjectLink
import io.antarescircuit.jabbah.graph.view.GraphElementView
import io.antarescircuit.jabbah.graph.view.GraphView

/** A [Command] for clearing the contents of an [Addressable].*/
class AddressableClearCommand(
	private val drawingView: DrawingView<GraphElementView<*>, GraphView>?,
	private val link: ObjectLink<Addressable>,
	private val bitWidth: BitWidth
) : AbstractCommand("antares.command.clearMemory"), Undoable {

	private val addressable: Addressable get() = link.getLinkedObject(drawingView?.drawing?.graph)

	private var oldContents: String? = null

	override fun execute() {
		oldContents = MemoryDump.write(addressable.memory, bitWidth)
		addressable.clear()
	}

	override fun undo() {
		MemoryDump.read(addressable.memory, oldContents!!)
		addressable.update()
	}
}