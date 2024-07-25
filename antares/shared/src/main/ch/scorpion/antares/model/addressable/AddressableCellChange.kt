package ch.scorpion.antares.model.addressable

import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.Undoable
import ch.scorpion.jabbah.edit.command.AbstractCommand
import ch.scorpion.jabbah.graph.model.vertice.VerticeLink
import ch.scorpion.jabbah.graph.view.GraphView

/** Represents the change of the value of an [Addressable] cell by the user.*/
data class AddressableCellChange(
	val address: Int,
	val origValue: ULong,
	val newValue: ULong
)

class AddressableCellChangeCommand(
	private val view: DrawingView<GraphView>,
	private val link: VerticeLink,
	private val changes: Collection<AddressableCellChange>
) : AbstractCommand("antares.command.memoryContents", null), Undoable {

	private val addressable: Addressable get() = link.getLinkedVertice(view.drawing.graph!!) as Addressable

	override fun execute() {
		changes.forEach { addressable.setDataAt(it.address, it.newValue, null) }
	}

	override fun undo() {
		changes.forEach { addressable.setDataAt(it.address, it.origValue, null) }
	}
}