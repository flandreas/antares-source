package ch.scorpion.jabbah.graph.ui.usecase

import ch.scorpion.jabbah.app.ApplicationDataHolder
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import ch.scorpion.jabbah.graph.GraphApplicationContextHolder
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.app.ApplicationModeHolder
import ch.scorpion.jabbah.graph.ui.AbstractApplicationDataEditModeAction
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.Usecase
import ch.scorpion.jabbah.graph.view.app.UsecaseAppService
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.usecase.UsecaseImpl
import ch.scorpion.jabbah.graph.view.usecase.UsecaseRunner
import ch.scorpion.jabbah.graph.view.usecase.UsecaseTestRunner
import java.awt.Frame
import javax.swing.JOptionPane

abstract class AbstractUsecaseAction(
	baseName: String,
	applicationDataHolder: ApplicationDataHolder,
	applicationModeHolder: ApplicationModeHolder,
	protected val service: UsecaseAppService = GraphViewModule.usecaseAppService,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractApplicationDataEditModeAction(baseName, applicationDataHolder, applicationModeHolder, eventBus) {

	protected var usecase: Usecase? = null

	private val usecaseSelectionHandler: EventHandler<UsecaseSelectionEvent> = {
		usecase = it.usecase
		updateEnabled()
	}

	init {
		eventBus.register(UsecaseSelectionEvent::class, usecaseSelectionHandler)
	}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(usecaseSelectionHandler)
	}

	protected val graphView: GraphView? get() =
		(applicationDataHolder.data!!.content as? MetaGraph)?.graph?.graphView
}

/** Asks the user for the name of a new [Usecase] and adds it to the current [GraphView].*/
class AddUsecaseAction(
	applicationDataHolder: ApplicationDataHolder,
	applicationModeHolder: ApplicationModeHolder,
	service: UsecaseAppService = GraphViewModule.usecaseAppService,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractUsecaseAction("usecases.action.addUsecase", applicationDataHolder, applicationModeHolder, service, eventBus) {

	override fun execute(event: ActionEvent) {
		val name = JOptionPane.showInputDialog(
			Frame.getFrames()[0],
			Translations.getString("usecases.action.addUsecase.question"),
			name,
			JOptionPane.QUESTION_MESSAGE
		)
		if (StringUtils.isEmpty(name)) {
			return
		}

		service.addUsecase(applicationDataHolder, UsecaseImpl(name))
	}

	override fun calculateEnabled(): Boolean =
		super.calculateEnabled() && usecase == null
}

/** Deletes the currently selected [Usecase].*/
class DeleteUsecaseAction(
	applicationDataHolder: ApplicationDataHolder,
	applicationModeHolder: ApplicationModeHolder,
	service: UsecaseAppService = GraphViewModule.usecaseAppService,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractUsecaseAction("usecases.action.deleteUsecase", applicationDataHolder, applicationModeHolder, service, eventBus) {

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
	applicationDataHolder: ApplicationDataHolder,
	private val scheduler: Scheduler,
	service: UsecaseAppService = GraphViewModule.usecaseAppService,
	eventBus: EventBus = BaseModule.eventBus,
	applicationModeHolder: ApplicationModeHolder
) : AbstractUsecaseAction("usecases.action.runUsecase", applicationDataHolder, applicationModeHolder, service, eventBus) {

	override fun execute(event: ActionEvent) {
		usecase?.let {
			UsecaseRunner(it, graphView!!, scheduler, applicationModeHolder).run()
		}
	}

	override fun calculateEnabled(): Boolean =
		super.calculateEnabled() && usecase != null
}

/** Executes the test script of the currently selected [Usecase].*/
class RunSingleUsecaseTestAction(
	applicationDataHolder: ApplicationDataHolder,
	private val scheduler: Scheduler,
	service: UsecaseAppService = GraphViewModule.usecaseAppService,
	eventBus: EventBus = BaseModule.eventBus,
	applicationModeHolder: ApplicationModeHolder
) : AbstractUsecaseAction("usecaseTest.action.runSingleTest", applicationDataHolder, applicationModeHolder, service, eventBus) {

	override fun execute(event: ActionEvent) {
		usecase?.let {
			UsecaseTestRunner(listOf(it), graphView!!, scheduler,applicationModeHolder).run()
		}
	}

	override fun calculateEnabled(): Boolean =
		super.calculateEnabled() && usecase != null && usecase!!.testScript != null
}

class RunAllTestsAction(
	applicationDataHolder: ApplicationDataHolder,
	private val scheduler: Scheduler,
	service: UsecaseAppService = GraphViewModule.usecaseAppService,
	eventBus: EventBus = BaseModule.eventBus,
	applicationModeHolder: ApplicationModeHolder
) : AbstractUsecaseAction("usecaseTest.action.runAllTests", applicationDataHolder, applicationModeHolder, service, eventBus) {

	override fun execute(event: ActionEvent) {
		UsecaseTestRunner(graphView!!.usecases.withTests(), graphView!!, scheduler, applicationModeHolder).run()
	}

	override fun calculateEnabled(): Boolean =
		super.calculateEnabled() && graphView?.usecases?.hasTest == true
}

class RecordUsecaseAction(
	applicationDataHolder: ApplicationDataHolder,
	applicationModeHolder: ApplicationModeHolder,
	private val graphAppContextHolder: GraphApplicationContextHolder,
	service: UsecaseAppService = GraphViewModule.usecaseAppService,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractUsecaseAction("usecase.action.record", applicationDataHolder, applicationModeHolder, service, eventBus) {

	override fun calculateEnabled(): Boolean =
		super.calculateEnabled() && usecase != null

	override fun execute(event: ActionEvent) {
		RecordUsecasePanel.showAsDialog(usecase!!, applicationDataHolder, applicationModeHolder, graphAppContextHolder, service)
	}
}