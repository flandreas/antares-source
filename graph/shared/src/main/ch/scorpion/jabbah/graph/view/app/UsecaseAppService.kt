package ch.scorpion.jabbah.graph.view.app

import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.app.ApplicationData
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.edit.CommandManager
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.Usecase
import ch.scorpion.jabbah.graph.view.usecase.AddUsecaseCommand
import ch.scorpion.jabbah.graph.view.usecase.DeleteUsecaseCommand

class UsecaseAppService(
	private val commandManager: CommandManager = EditModule.commandManager
) {
	companion object {
		private val LOG by logger(UsecaseAppService::class)
	}

	private fun graphView(application: Application): GraphView = (application.controller.data!!.content as MetaGraph).graph.graphView

	/**
	 * Creates a clone of [usecase] and adds it to the [GraphView] in the [ApplicationData] of [application].
	 * @return the ID of the cloned [Usecase]
	 */
	fun addUsecase(application: Application, usecase: Usecase): Int {
		LOG.debug("Add new Usecase to GraphView ${graphView(application).graph?.uuid}")
		val command = AddUsecaseCommand(application.controller, usecase)
		commandManager.execute(command)
		return command.addedUsecaseId
	}

	/** Deletes the [Usecase] with the specified ID from the [GraphView] in the [ApplicationData] of [application]. */
	fun deleteUsecase(application: Application, usecaseId: Int) {
		LOG.debug("Delete Usecase $usecaseId from GraphView ${graphView(application).graph?.uuid}")
		commandManager.execute(DeleteUsecaseCommand(application.controller, usecaseId))
	}

	/** Duplicates the [Usecase] with the specified ID from the [GraphView] in the [ApplicationData] of [application]. */
	fun duplicateUsecase(application: Application, usecaseId: Int, newName: String): Int {
		LOG.debug("Duplicate Usecase in GraphView ${graphView(application).graph?.uuid}")
		val duplicate = graphView(application).usecases.get(usecaseId).duplicate(newName)
		val command = AddUsecaseCommand(application.controller, duplicate)
		commandManager.execute(command)
		return command.addedUsecaseId
	}
}