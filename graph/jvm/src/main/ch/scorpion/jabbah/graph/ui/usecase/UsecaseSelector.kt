package ch.scorpion.jabbah.graph.ui.usecase

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import ch.scorpion.jabbah.execution.scheduler.SchedulerActivationStateEvent
import ch.scorpion.jabbah.graph.app.ApplicationModeHolder
import ch.scorpion.jabbah.graph.ui.graphpanel.EditedGraphViewEvent
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.Usecase
import ch.scorpion.jabbah.graph.view.UsecaseAddedEvent
import ch.scorpion.jabbah.graph.view.UsecaseRemovedEvent
import ch.scorpion.jabbah.graph.view.usecase.UsecaseRunner
import java.awt.Component
import javax.swing.DefaultComboBoxModel
import javax.swing.JComboBox
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.plaf.basic.BasicComboBoxRenderer

/**
 * Allows the user to select a [Usecase] to run it.
 */
class UsecaseSelector(
	private val scheduler: Scheduler,
	private val applicationModeHolder: ApplicationModeHolder,
	private val eventBus: EventBus = BaseModule.eventBus
) : JComboBox<Usecase>() {

	private var graphView: GraphView? = null
	private val editedGraphViewHandler: EventHandler<EditedGraphViewEvent> = { handle(it) }
	private val schedulerActivationStateHandler: EventHandler<SchedulerActivationStateEvent> = { handle(it) }
	private val usecaseAddedHandler: EventHandler<UsecaseAddedEvent> = {
		if (it.graphView === graphView) { fillUsecases() }
	}
	private val usecaseDeletedHandler: EventHandler<UsecaseRemovedEvent> = {
		if (it.graphView === graphView) { fillUsecases() }
	}

	init {
		eventBus.register(EditedGraphViewEvent::class, editedGraphViewHandler)
		eventBus.register(SchedulerActivationStateEvent::class, schedulerActivationStateHandler)
		eventBus.register(UsecaseAddedEvent::class, usecaseAddedHandler)
		eventBus.register(UsecaseRemovedEvent::class, usecaseDeletedHandler)

		renderer = UsecaseModelRenderer()
		addActionListener {
			if (selectedItem != null) {
				runUsecase(selectedItem as Usecase)
			}
		}


		fillUsecases(listOf())
	}

	fun dispose() {
		eventBus.unregister(editedGraphViewHandler)
		eventBus.unregister(schedulerActivationStateHandler)
		eventBus.unregister(usecaseAddedHandler)
		eventBus.unregister(usecaseDeletedHandler)
	}

	private fun fillUsecases() {
		graphView?.let { fillUsecases(it.usecases.getUsecases()) }
	}

	private fun fillUsecases(usecases: Iterable<Usecase>) {
		val model = DefaultComboBoxModel<Usecase>()
		model.addElement(null)
		usecases.forEach { model.addElement(it) }
		this.model = model
		maximumSize = preferredSize

		updateEnabledness()
	}

	private fun runUsecase(usecase: Usecase) {
		// Reload the Usecase in case the script has changed
		graphView?.let { gv ->
			UsecaseRunner(gv.usecases.get(usecase.id), gv, scheduler, applicationModeHolder).run()
		}
	}

	private fun handle(event: EditedGraphViewEvent) {
		if (event.newGraphView == null || event.newGraphView.usecases.isEmpty) {
			fillUsecases(listOf())
		} else {
			fillUsecases(event.newGraphView.usecases.getUsecases())
		}
		graphView = event.newGraphView
	}

	private fun handle(@Suppress("UNUSED_PARAMETER") event: SchedulerActivationStateEvent) {
		if (event.scheduler === scheduler) {
			updateEnabledness()
			if (!scheduler.isActive) {
				selectedIndex = 0
			}
		}
	}

	private fun updateEnabledness() {
		isEnabled = model.size > 1 && !scheduler.isActive
	}

	private class UsecaseModelRenderer : BasicComboBoxRenderer() {

		override fun getListCellRendererComponent(list: JList<*>?, value: Any?, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component {
			val renderer = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus) as JLabel
			if (value == null) {
				renderer.text = Translations.getString("usecase.selectUsecase.text")
			}
			return renderer
		}
	}
}