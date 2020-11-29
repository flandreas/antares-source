package ch.scorpion.jabbah.graph.view.usecase

import ch.scorpion.jabbah.edit.Undoable
import ch.scorpion.jabbah.edit.UndoableDataHolder
import ch.scorpion.jabbah.edit.command.AbstractCommand
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.Usecase
import ch.scorpion.jabbah.io.StorableCloner

/** Adds a clone of a [Usecase] to a [GraphView].*/
class AddUsecaseCommand(
	private val dataHolder: UndoableDataHolder,
	private val usecase: Usecase
) : AbstractCommand("usecase.command.add"), Undoable {

	private val graphView: GraphView get() = (dataHolder.getUndoableState() as MetaGraph).graph.graphView
	var addedUsecaseId: Int = 0
		private set

	override fun execute() {
		val clone = StorableCloner.clone(usecase)
		graphView.usecases.add(clone)
		addedUsecaseId = clone.id
	}

	override fun undo() {
		graphView.usecases.remove(addedUsecaseId)
	}
}

/**
 * Removes a [Usecase] from a [GraphView] and deletes it.
 * By intention not [Undoable] to avoid the need to store a clone.
 */
class DeleteUsecaseCommand(
	private val dataHolder: UndoableDataHolder,
	private val usecaseId: Int
) : AbstractCommand("usecase.command.delete") {

	private val graphView: GraphView get() = (dataHolder.getUndoableState() as MetaGraph).graph.graphView

	override fun execute() {
		graphView.usecases.remove(usecaseId)
	}
}