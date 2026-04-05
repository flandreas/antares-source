package io.antarescircuit.antares.model.testcase

import io.antarescircuit.antares.model.DigitalGraph
import io.antarescircuit.jabbah.app.ApplicationDataHolder
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.edit.CommandManager
import io.antarescircuit.jabbah.edit.module.EditModule
import io.antarescircuit.jabbah.graph.MetaGraph
import io.antarescircuit.jabbah.graph.view.GraphView

class TestcaseAppService(
	private val commandManager: CommandManager = EditModule.commandManager
) {

	companion object {
		private val LOG by logger(TestcaseAppService::class)
	}

	private fun graphView(applicationDataHolder: ApplicationDataHolder): GraphView = (applicationDataHolder.data!!.content as MetaGraph).graph.graphView

	fun addTestcase(dataHolder: ApplicationDataHolder, testcase: Testcase): Int {
		LOG.userTrail("Add new Testcase to Graph ${graphView(dataHolder).graph?.uuid}")
		val command = AddTestcaseCommand(dataHolder, testcase)
		commandManager.execute(command)
		return command.addedTestcaseId
	}

	fun deleteTestcase(dataHolder: ApplicationDataHolder, testcaseId: Int) {
		LOG.userTrail("Delete Testcase $testcaseId from Graph ${graphView(dataHolder).graph?.uuid}")
		commandManager.execute(DeleteTestcaseCommand(dataHolder, testcaseId))
	}

	fun duplicateTestcase(dataHolder: ApplicationDataHolder, testcaseId: Int, newName: String): Int {
		LOG.userTrail("Duplicate Testcase $testcaseId in Graph ${graphView(dataHolder).graph?.uuid}")
		val duplicate = (graphView(dataHolder).graph as DigitalGraph).testcases.get(testcaseId).duplicate(newName)
		val command = AddTestcaseCommand(dataHolder, duplicate, "antares.testcase.command.duplicate")
		commandManager.execute(command)
		return command.addedTestcaseId
	}

	fun moveTestcase(dataHolder: ApplicationDataHolder, testcaseId: Int, newIndex: Int) {
		LOG.userTrail("Move Testcase $testcaseId to new index $newIndex in Graph ${graphView(dataHolder).graph?.uuid}")
		commandManager.execute(MoveTestcaseCommand(dataHolder, testcaseId, newIndex))
	}
}