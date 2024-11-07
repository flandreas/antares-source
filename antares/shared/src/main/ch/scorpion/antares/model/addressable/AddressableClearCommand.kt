package ch.scorpion.antares.model.addressable

import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.jabbah.edit.Command
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.Undoable
import ch.scorpion.jabbah.graph.app.AbstractGraphViewCommand
import ch.scorpion.jabbah.graph.model.vertice.VerticeLink
import ch.scorpion.jabbah.graph.view.GraphView

/** A [Command] for clearing the contents of an [Addressable].*/
class AddressableClearCommand(
	view: DrawingView<GraphView>,
	private val link: VerticeLink,
	private val bitWidth: BitWidth
) : AbstractGraphViewCommand("antares.command.clearMemory", view), Undoable {

	private val addressable: Addressable get() = link.getLinkedVertice(drawingView.drawing.graph!!) as Addressable

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