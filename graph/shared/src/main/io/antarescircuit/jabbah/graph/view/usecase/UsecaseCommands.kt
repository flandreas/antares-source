package io.antarescircuit.jabbah.graph.view.usecase

import io.antarescircuit.jabbah.edit.Undoable
import io.antarescircuit.jabbah.edit.UndoableDataHolder
import io.antarescircuit.jabbah.edit.command.AbstractCommand
import io.antarescircuit.jabbah.edit.model.text.ScriptProperty
import io.antarescircuit.jabbah.graph.MetaGraph
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.Usecase
import io.antarescircuit.jabbah.io.StorableCloner

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

class RecordUsecaseCommand(
	private val dataHolder: UndoableDataHolder,
	private val usecaseId: Int,
	private val script: String
) : AbstractCommand("usecase.command.record") {

	private val graphView: GraphView get() = (dataHolder.getUndoableState() as MetaGraph).graph.graphView

	override fun execute() {
		graphView.usecases.get(usecaseId).executionScript = ScriptProperty(script)
	}
}