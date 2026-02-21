package ch.scorpion.antares.model.testcase

import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.jabbah.edit.Undoable
import ch.scorpion.jabbah.edit.UndoableDataHolder
import ch.scorpion.jabbah.edit.command.AbstractCommand
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.io.StorableCloner

class AddTestcaseCommand(
	private val dataHolder: UndoableDataHolder,
	private val testcase: Testcase,
	descriptionKey: String = "antares.testcase.command.add"
) : AbstractCommand(descriptionKey), Undoable {

	private val graphView: GraphView get() = (dataHolder.getUndoableState() as MetaGraph).graph.graphView

	var addedTestcaseId: Int = 0
		private set

	override fun execute() {
		val clone = StorableCloner.clone(testcase)
		(graphView.graph as DigitalGraph?)?.testcases?.add(clone)
		addedTestcaseId = clone.id
	}

	override fun undo() {
		(graphView.graph as DigitalGraph?)?.testcases?.remove(addedTestcaseId)
	}
}

class DeleteTestcaseCommand(
	private val dataHolder: UndoableDataHolder,
	private val testcaseId: Int
) : AbstractCommand("antares.testcase.command.delete") {

	private val graphView: GraphView get() = (dataHolder.getUndoableState() as MetaGraph).graph.graphView

	override fun execute() {
		(graphView.graph as DigitalGraph).testcases.remove(testcaseId)
	}
}

class MoveTestcaseCommand(
	private val dataHolder: UndoableDataHolder,
	private val testcaseId: Int,
	private val newIndex: Int
) : AbstractCommand("antares.testcase.command.move"), Undoable {

	private val graphView: GraphView get() = (dataHolder.getUndoableState() as MetaGraph).graph.graphView
	private val digitalGraph: DigitalGraph get() = graphView.graph as DigitalGraph

	private val oldIndex: Int = digitalGraph.testcases.indexOfTestcase(testcaseId)

	override fun execute() {
		digitalGraph.testcases.move(testcaseId, newIndex)
	}

	override fun undo() {
		val effIndex = if (oldIndex > newIndex) oldIndex + 1 else oldIndex
		digitalGraph.testcases.move(testcaseId, effIndex)
	}
}