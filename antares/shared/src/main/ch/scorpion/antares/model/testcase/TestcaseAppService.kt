package ch.scorpion.antares.model.testcase

import ch.scorpion.jabbah.app.ApplicationDataHolder
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.edit.CommandManager
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.view.GraphView

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
}