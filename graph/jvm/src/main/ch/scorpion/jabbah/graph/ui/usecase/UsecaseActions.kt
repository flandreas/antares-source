package ch.scorpion.jabbah.graph.ui.usecase

import ch.scorpion.jabbah.app.CurrentSavableEvent
import ch.scorpion.jabbah.app.Savable
import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.CommandManager
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.execution.module.ExecutionModule
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import ch.scorpion.jabbah.graph.ui.EditedGraphViewEvent
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.Usecase
import ch.scorpion.jabbah.graph.view.usecase.*
import java.awt.Frame
import javax.swing.JOptionPane

abstract class AbstractUsecaseAction(
	baseName: String,
	protected val cmdManager: CommandManager = EditModule.commandManager,
	private val eventBus: EventBus = BaseModule.eventBus
) : AbstractAction(baseName) {

	private var currentSavable: Savable? = null
	protected var graphView: GraphView? = null
	protected var usecase: Usecase? = null

	private val editedGraphViewHandler: EventHandler<EditedGraphViewEvent> = {
		graphView = it.newGraphView
		updateEnabledness()
	}

	private val usecaseSelectionHandler: EventHandler<UsecaseSelectionEvent> = {
		graphView = it.graphView
		usecase = it.usecase
		updateEnabledness()
	}

	private val currentSavableHandler: EventHandler<CurrentSavableEvent> = {
		currentSavable = it.savable
		updateEnabledness()
	}

	init {
		eventBus.register(EditedGraphViewEvent::class, editedGraphViewHandler)
		eventBus.register(UsecaseSelectionEvent::class, usecaseSelectionHandler)
		eventBus.register(CurrentSavableEvent::class, currentSavableHandler)
		enabled = false
	}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(editedGraphViewHandler)
		eventBus.unregister(usecaseSelectionHandler)
		eventBus.unregister(currentSavableHandler)
	}

	protected fun updateEnabledness() {
		enabled = calculateEnabled()
	}

	protected open fun calculateEnabled(): Boolean {
		return graphView != null && !(currentSavable?.readOnly ?: false)
	}
}

/** Asks the user for the name of a new [Usecase] and adds it to the current [GraphView].*/
class AddUsecaseAction : AbstractUsecaseAction("usecases.action.addUsecase") {

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
		cmdManager.execute(AddUsecaseCommand(graphView!!, UsecaseImpl(name)))
	}

	override fun calculateEnabled(): Boolean {
		return super.calculateEnabled() && usecase == null
	}
}

/** Deletes the currently selected [Usecase].*/
class DeleteUsecaseAction : AbstractUsecaseAction("usecases.action.deleteUsecase") {

	override fun execute(event: ActionEvent) {
		if (JOptionPane.showConfirmDialog(
				Frame.getFrames()[0],
				Translations.getString("usecases.action.deleteUsecase.question", usecase!!.name.value),
				Translations.getString("usecases.action.deleteUsecase.name"),
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION) {
			cmdManager.execute(DeleteUsecaseCommand(graphView!!, usecase!!))
		}
	}

	override fun calculateEnabled(): Boolean {
		return super.calculateEnabled() && usecase != null
	}
}

class RunUsecaseAction(
	private val scheduler: Scheduler = ExecutionModule.scheduler
) : AbstractUsecaseAction("usecases.action.runUsecase") {

	override fun execute(event: ActionEvent) {
		usecase?.let {
			UsecaseRunner(it, graphView!!, scheduler).run()
		}
	}

	override fun calculateEnabled(): Boolean {
		return super.calculateEnabled() && usecase != null
	}
}

/** Executes the test script of the currently selected [Usecase].*/
class RunSingleUsecaseTestAction(
	private val scheduler: Scheduler = ExecutionModule.scheduler
) : AbstractUsecaseAction("usecaseTest.action.runSingleTest") {

	override fun execute(event: ActionEvent) {
		usecase?.let {
			UsecaseTestRunner(listOf(it), graphView!!, scheduler).run()
		}
	}

	override fun calculateEnabled(): Boolean {
		return super.calculateEnabled() && usecase != null && usecase!!.testScript != null
	}
}

class RunAllTestsAction(
	private val scheduler: Scheduler = ExecutionModule.scheduler
) : AbstractUsecaseAction("usecaseTest.action.runAllTests") {

	override fun execute(event: ActionEvent) {
		UsecaseTestRunner(graphView!!.usecases.withTests(), graphView!!, scheduler).run()
	}

	override fun calculateEnabled(): Boolean {
		return super.calculateEnabled() && graphView!!.usecases.hasTest
	}
}