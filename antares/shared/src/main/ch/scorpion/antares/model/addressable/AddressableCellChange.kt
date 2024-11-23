package ch.scorpion.antares.model.addressable

import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.Undoable
import ch.scorpion.jabbah.edit.command.AbstractCommand
import ch.scorpion.jabbah.graph.model.vertice.ObjectLink
import ch.scorpion.jabbah.graph.view.GraphView

/** Represents the change of the value of an [Addressable] cell by the user.*/
data class AddressableCellChange(
	val address: Int,
	val origValue: ULong,
	val newValue: ULong
)

class AddressableCellChangeCommand(
	private val drawingView: DrawingView<GraphView>?,
	private val link: ObjectLink<Addressable>,
	private val changes: Collection<AddressableCellChange>
) : AbstractCommand("antares.command.memoryContents"), Undoable {

	private val addressable: Addressable get() = link.getLinkedObject(drawingView?.drawing?.graph)

	override fun execute() {
		changes.forEach { addressable.setDataAt(it.address, it.newValue, null) }
	}

	override fun undo() {
		changes.forEach { addressable.setDataAt(it.address, it.origValue, null) }
	}
}