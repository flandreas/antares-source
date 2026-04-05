package io.antarescircuit.antares.model.addressable

import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.Undoable
import io.antarescircuit.jabbah.edit.command.AbstractCommand
import io.antarescircuit.jabbah.graph.model.vertice.ObjectLink
import io.antarescircuit.jabbah.graph.view.GraphView

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