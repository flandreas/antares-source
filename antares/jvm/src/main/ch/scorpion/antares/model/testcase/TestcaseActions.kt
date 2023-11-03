package ch.scorpion.antares.model.testcase

import ch.scorpion.antares.model.module.AntaresModelModule
import ch.scorpion.antares.model.testcase.Testcase
import ch.scorpion.antares.model.testcase.TestcaseAppService
import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.app.ApplicationModeHolder
import ch.scorpion.jabbah.graph.ui.AbstractApplicationDataEditModeAction
import java.awt.Frame
import javax.swing.JOptionPane

abstract class AbstractTestcaseAction(
	baseName: String,
	application: Application,
	applicationModeHolder: ApplicationModeHolder,
	protected val service: TestcaseAppService = AntaresModelModule.testcaseAppService,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractApplicationDataEditModeAction(baseName, application, applicationModeHolder, eventBus) {

	protected var testcase: Testcase? = null

	private val testcaseSelectionHandler: EventHandler<TestcaseSelectionEvent> = {
		testcase = it.testcase
		updateEnabled()
	}

	init {
		eventBus.register(TestcaseSelectionEvent::class, testcaseSelectionHandler)
	}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(testcaseSelectionHandler)
	}
}

class AddTestcaseAction(
	application: Application,
	applicationModeHolder: ApplicationModeHolder,
	service: TestcaseAppService = AntaresModelModule.testcaseAppService,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractTestcaseAction("antares.testcase.action.add", application, applicationModeHolder, service, eventBus) {

	override fun execute(event: ActionEvent) {
		val name = JOptionPane.showInputDialog(
			Frame.getFrames()[0],
			Translations.getString("antares.testcase.action.add.question"),
			name,
			JOptionPane.QUESTION_MESSAGE,
			null,
			null,
			"Test"
		) as String?

		if (StringUtils.isEmpty(name)) {
			return
		}

		service.addTestcase(application, Testcase(name!!))
	}

	override fun calculateEnabled(): Boolean =
		super.calculateEnabled() && testcase == null
}

class DeleteTestcaseAction(
	application: Application,
	applicationModeHolder: ApplicationModeHolder,
	service: TestcaseAppService = AntaresModelModule.testcaseAppService,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractTestcaseAction("antares.testcase.action.delete", application, applicationModeHolder, service, eventBus) {

	override fun execute(event: ActionEvent) {
		if (JOptionPane.showConfirmDialog(
				Frame.getFrames()[0],
				Translations.getString("antares.testcase.action.delete.question", testcase!!.name.value),
				Translations.getString("antares.testcase.action.delete.name"),
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION)
		{
			service.deleteTestcase(application, testcase!!.id)
		}
	}
}