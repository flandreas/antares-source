package ch.scorpion.antares.model.addressable

import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.Undoable
import ch.scorpion.jabbah.edit.command.AbstractCommand
import ch.scorpion.jabbah.graph.model.vertice.ObjectLink
import ch.scorpion.jabbah.graph.view.GraphView

data class AddressableCommentChange(
	val address: Int,
	val oldValue: String?,
	val newValue: String?
)

class AddressableCommentChangeCommand(
	private val drawingView: DrawingView<GraphView>?,
	private val link: ObjectLink<Addressable>,
	private val changes: Collection<AddressableCommentChange>
) : AbstractCommand("antares.command.memoryContents"), Undoable {

	private val addressable: Addressable get() = link.getLinkedObject(drawingView?.drawing?.graph)

	override fun execute() {
		changes.forEach { addressable.setCommentAt(it.address, it.newValue, null) }
	}

	override fun undo() {
		changes.forEach { addressable.setCommentAt(it.address, it.oldValue, null) }
	}
}