package ch.scorpion.antares.model.testcase

import ch.scorpion.jabbah.app.Application
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

	private fun graphView(application: Application): GraphView = (application.controller.data!!.content as MetaGraph).graph.graphView

	fun addTestcase(application: Application, testcase: Testcase): Int {
		LOG.userTrail("Add new Testcase to Graph ${graphView(application).graph?.uuid}")
		val command = AddTestcaseCommand(application.controller, testcase)
		commandManager.execute(command)
		return command.addedTestcaseId
	}

	fun deleteTestcase(application: Application, testcaseId: Int) {
		LOG.userTrail("Delete Testcase $testcaseId from Graph ${graphView(application).graph?.uuid}")
		commandManager.execute(DeleteTestcaseCommand(application.controller, testcaseId))
	}
}