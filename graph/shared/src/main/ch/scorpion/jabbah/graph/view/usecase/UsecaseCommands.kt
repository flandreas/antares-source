package ch.scorpion.jabbah.graph.view.usecase

import ch.scorpion.jabbah.edit.command.AbstractCommand
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.Usecase

/** Adds a [Usecase] to a [GraphView].*/
class AddUsecaseCommand(
	private val graphView: GraphView<*>,
	private val usecase: Usecase
) : AbstractCommand("usecase.command.add") {

	override fun execute() {
		graphView.usecases.add(usecase)
	}

	override fun undo() {
		graphView.usecases.remove(usecase)
	}
}

/** Removes a [Usecase] from a [GraphView] and deletes it.*/
class DeleteUsecaseCommand(
	private val graphView: GraphView<*>,
	private val usecase: Usecase
) : AbstractCommand("usecase.command.delete") {

	override fun execute() {
		graphView.usecases.remove(usecase)
	}

	override fun undo() {
		graphView.usecases.add(usecase)
	}
}