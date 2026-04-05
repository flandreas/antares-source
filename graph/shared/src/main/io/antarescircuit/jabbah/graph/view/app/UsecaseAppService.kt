package io.antarescircuit.jabbah.graph.view.app

import io.antarescircuit.jabbah.app.ApplicationData
import io.antarescircuit.jabbah.app.ApplicationDataHolder
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.edit.CommandManager
import io.antarescircuit.jabbah.edit.module.EditModule
import io.antarescircuit.jabbah.graph.MetaGraph
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.Usecase
import io.antarescircuit.jabbah.graph.view.usecase.AddUsecaseCommand
import io.antarescircuit.jabbah.graph.view.usecase.DeleteUsecaseCommand
import io.antarescircuit.jabbah.graph.view.usecase.RecordUsecaseCommand

class UsecaseAppService(
	private val commandManager: CommandManager = EditModule.commandManager
) {
	companion object {
		private val LOG by logger(UsecaseAppService::class)
	}

	private fun graphView(dataHolder: ApplicationDataHolder): GraphView = (dataHolder.data!!.content as MetaGraph).graph.graphView

	/**
	 * Creates a clone of [usecase] and adds it to the [GraphView] in the [ApplicationData] of [application].
	 * @return the ID of the cloned [Usecase]
	 */
	fun addUsecase(dataHolder: ApplicationDataHolder, usecase: Usecase): Int {
		LOG.userTrail("Add new Usecase to GraphView ${graphView(dataHolder).graph?.uuid}")
		val command = AddUsecaseCommand(dataHolder, usecase)
		commandManager.execute(command)
		return command.addedUsecaseId
	}

	/** Deletes the [Usecase] with the specified ID from the [GraphView] in the [ApplicationData] of [application]. */
	fun deleteUsecase(dataHolder: ApplicationDataHolder, usecaseId: Int) {
		LOG.userTrail("Delete Usecase $usecaseId from GraphView ${graphView(dataHolder).graph?.uuid}")
		commandManager.execute(DeleteUsecaseCommand(dataHolder, usecaseId))
	}

	/** Duplicates the [Usecase] with the specified ID from the [GraphView] in the [ApplicationData] of [application]. */
	fun duplicateUsecase(dataHolder: ApplicationDataHolder, usecaseId: Int, newName: String): Int {
		LOG.userTrail("Duplicate Usecase in GraphView ${graphView(dataHolder).graph?.uuid}")
		val duplicate = graphView(dataHolder).usecases.get(usecaseId).duplicate(newName)
		val command = AddUsecaseCommand(dataHolder, duplicate)
		commandManager.execute(command)
		return command.addedUsecaseId
	}

	fun recordUsecase(dataHolder: ApplicationDataHolder, usecaseId: Int, script: String) {
		LOG.userTrail("Record usecase in GraphView ${graphView(dataHolder).graph?.uuid}")
		val command = RecordUsecaseCommand(dataHolder, usecaseId, script)
		commandManager.execute(command)
	}
}