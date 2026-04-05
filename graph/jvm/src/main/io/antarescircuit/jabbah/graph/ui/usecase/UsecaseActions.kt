package io.antarescircuit.jabbah.graph.ui.usecase

import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.graph.view.Usecase
import io.antarescircuit.jabbah.graph.view.app.UsecaseAppService
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule
import io.antarescircuit.jabbah.graph.view.usecase.UsecaseRunner
import io.antarescircuit.jabbah.graph.view.usecase.UsecaseTestRunner
import java.awt.Frame
import javax.swing.JOptionPane

/** Deletes the currently selected [Usecase].*/
class DeleteUsecaseAction(
	controller: UsecaseViewController,
	service: UsecaseAppService = GraphViewModule.usecaseAppService,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractUsecaseAction(controller,"usecases.action.deleteUsecase", service, eventBus) {

	override val opensDialog: Boolean get() = true

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

	override val opensDialog: Boolean get() = true

	override fun calculateEnabled(): Boolean =
		super.calculateEnabled() && usecase != null

	override fun execute(event: ActionEvent) {
		RecordUsecasePanel.showAsDialog(usecase!!, applicationDataHolder, applicationModeHolder, controller.applicationContextHolder, service)
	}
}