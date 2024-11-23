package ch.scorpion.antares.model.addressable

import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.jabbah.edit.Command
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.Undoable
import ch.scorpion.jabbah.edit.command.AbstractCommand
import ch.scorpion.jabbah.graph.model.vertice.ObjectLink
import ch.scorpion.jabbah.graph.view.GraphView

/** A [Command] for clearing the contents of an [Addressable].*/
class AddressableClearCommand(
	private val drawingView: DrawingView<GraphView>?,
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