package ch.scorpion.antares.model.addressable

import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.Undoable
import ch.scorpion.jabbah.edit.command.AbstractCommand
import ch.scorpion.jabbah.graph.model.vertice.VerticeLink
import ch.scorpion.jabbah.graph.view.GraphView

data class AddressableCommentChange(
	val address: Int,
	val oldValue: String?,
	val newValue: String?
)

class AddressableCommentChangeCommand(
	private val view: DrawingView<GraphView>,
	private val link: VerticeLink,
	private val changes: Collection<AddressableCommentChange>
) : AbstractCommand("antares.command.memoryContents", null), Undoable {

	private val addressable: Addressable get() = link.getLinkedVertice(view.drawing.graph!!) as Addressable

	override fun execute() {
		changes.forEach { addressable.setCommentAt(it.address, it.newValue, null) }
	}

	override fun undo() {
		changes.forEach { addressable.setCommentAt(it.address, it.oldValue, null) }
	}
}