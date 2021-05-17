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
		LOG.trace("Add new Usecase to GraphView ${graphView(application).graph?.uuid}")
		val command = AddUsecaseCommand(application.controller, usecase)
		commandManager.execute(command)
		return command.addedUsecaseId
	}

	/** Deletes the [Usecase] with the specified ID from the [GraphView] in the [ApplicationData] of [application]. */
	fun deleteUsecase(application: Application, usecaseId: Int) {
		LOG.trace("Delete Usecase $usecaseId from GraphView ${graphView(application).graph?.uuid}")
		commandManager.execute(DeleteUsecaseCommand(application.controller, usecaseId))
	}
}