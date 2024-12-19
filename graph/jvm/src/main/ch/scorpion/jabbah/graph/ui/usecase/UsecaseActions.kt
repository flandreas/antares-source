package ch.scorpion.jabbah.graph.ui.usecase

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.view.Usecase
import ch.scorpion.jabbah.graph.view.app.UsecaseAppService
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.usecase.UsecaseRunner
import ch.scorpion.jabbah.graph.view.usecase.UsecaseTestRunner
import java.awt.Frame
import javax.swing.JOptionPane

/** Deletes the currently selected [Usecase].*/
class DeleteUsecaseAction(
	controller: UsecaseViewController,
	service: UsecaseAppService = GraphViewModule.usecaseAppService,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractUsecaseAction(controller,"usecases.action.deleteUsecase", service, eventBus) {

	override fun execute(event: ActionEvent) {
		if (JOptionPane.showConfirmDialog(
				Frame.getFrames()[0],
				Translations.getString("usecases.action.deleteUsecase.question", usecase!!.name.value),
				Translations.getString("usecases.action.deleteUsecase.name"),
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION)
		{
			service.deleteUsecase(applicationDataHolder, usecase!!.id)
		}
	}

	override fun calculateEnabled(): Boolean =
		super.calculateEnabled() && usecase != null
}

class RunUsecaseAction(
	controller: UsecaseViewController,
	service: UsecaseAppService = GraphViewModule.usecaseAppService,
	eventBus: EventBus = BaseModule.eventBus,
) : AbstractUsecaseAction(controller, "usecases.action.runUsecase", service, eventBus) {

	override fun execute(event: ActionEvent) {
		usecase?.let {
			UsecaseRunner(it, graphView!!, controller.applicationContextHolder.scheduler, applicationModeHolder).run()
		}
	}

	override fun calculateEnabled(): Boolean =
		super.calculateEnabled() && usecase != null
}

/** Executes the test script of the currently selected [Usecase].*/
class RunSingleUsecaseTestAction(
	controller: UsecaseViewController,
	service: UsecaseAppService = GraphViewModule.usecaseAppService,
	eventBus: EventBus = BaseModule.eventBus,
) : AbstractUsecaseAction(controller, "usecaseTest.action.runSingleTest", service, eventBus) {

	override fun execute(event: ActionEvent) {
		usecase?.let {
			UsecaseTestRunner(listOf(it), graphView!!, controller.applicationContextHolder.scheduler, applicationModeHolder).run()
		}
	}

	override fun calculateEnabled(): Boolean =
		super.calculateEnabled() && usecase != null && usecase!!.testScript != null
}

class RunAllTestsAction(
	controller: UsecaseViewController,
	service: UsecaseAppService = GraphViewModule.usecaseAppService,
	eventBus: EventBus = BaseModule.eventBus,
) : AbstractUsecaseAction(controller, "usecaseTest.action.runAllTests", service, eventBus) {

	override fun execute(event: ActionEvent) {
		UsecaseTestRunner(graphView!!.usecases.withTests(), graphView!!, controller.applicationContextHolder.scheduler, applicationModeHolder).run()
	}

	override fun calculateEnabled(): Boolean =
		super.calculateEnabled() && graphView?.usecases?.hasTest == true
}

class RecordUsecaseAction(
	controller: UsecaseViewController,
	service: UsecaseAppService = GraphViewModule.usecaseAppService,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractUsecaseAction(controller, "usecase.action.record", service, eventBus) {

	override fun calculateEnabled(): Boolean =
		super.calculateEnabled() && usecase != null

	override fun execute(event: ActionEvent) {
		RecordUsecasePanel.showAsDialog(usecase!!, applicationDataHolder, applicationModeHolder, controller.applicationContextHolder, service)
	}
}