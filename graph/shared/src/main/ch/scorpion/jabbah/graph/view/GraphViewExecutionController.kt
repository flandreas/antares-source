package ch.scorpion.jabbah.graph.view

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.execution.module.ExecutionModule
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import ch.scorpion.jabbah.execution.scheduler.SchedulerActivationStateEvent
import ch.scorpion.jabbah.graph.MetaGraphRepository
import ch.scorpion.jabbah.graph.app.ApplicationMode
import ch.scorpion.jabbah.graph.app.ApplicationModeEvent
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
import ch.scorpion.jabbah.graph.ui.GraphViewDisplayHandler
import ch.scorpion.jabbah.graph.ui.GraphViewExecutionHandler
import ch.scorpion.jabbah.graph.ui.GraphViewUI
import ch.scorpion.jabbah.graph.ui.GraphViewUsecaseExecutionHandler
import ch.scorpion.jabbah.graph.view.style.GraphTheme
import ch.scorpion.jabbah.io.IOModule
import ch.scorpion.jabbah.io.StorableCreator

/**
 * Uses by object that hold [GraphView]s to control starting and stopping
 * execution of these [GraphView]s and their [Graph]s.
 *
 * Listens for [SchedulerActivationStateEvent] and does the necessary controlling logic
 * such as binding the [Graph] and calling the callback methods.
 *
 * Organizes user input in the [DrawingView] differently depending on the current [ApplicationMode].
 */
class GraphViewExecutionController(
	private val graphViewUI: GraphViewUI,
	private val isRoot: Boolean,
	private val rootGraphProvider: () -> Graph?,
	private val graphViewsProvider: () -> Collection<GraphView>,
	private val scheduler: Scheduler = ExecutionModule.scheduler,
	private val repository: MetaGraphRepository = GraphModelModule.metaGraphRepository,
	private val storableCreator: StorableCreator = IOModule.storableCreator,
	private val eventBus: EventBus = BaseModule.eventBus
) {

	private val initialMode: ApplicationMode = if (scheduler.isActive) ApplicationMode.EXECUTE else ApplicationMode.EDIT

	private val schedulerActivationStateHandler: (SchedulerActivationStateEvent) -> Unit = { handle(it) }

	private val applicationModeEventHandler: (ApplicationModeEvent) -> Unit = { handle(it) }

	/** Forwards input events to the [GraphView] while executing.*/
	private val graphViewExecutionHandler = GraphViewExecutionHandler(graphViewUI.drawingView, scheduler, eventBus, initialMode)

	/** Forwards input events to the [GraphView] while displaying (i.e. NOT executing) and NOT being editable.*/
	private val graphViewDisplayHandler = GraphViewDisplayHandler(graphViewUI.drawingView, scheduler, eventBus)

	/** Forwards input events to the [GraphView] while a [Usecase] is executed.*/
	private val graphViewUsecaseExecutionHandler = GraphViewUsecaseExecutionHandler(graphViewUI.drawingView, scheduler, eventBus, initialMode)

	init {
		eventBus.register(SchedulerActivationStateEvent::class, schedulerActivationStateHandler)
		eventBus.register(ApplicationModeEvent::class, applicationModeEventHandler)
	}

	fun dispose() {
		eventBus.unregister(SchedulerActivationStateEvent::class, schedulerActivationStateHandler)
		eventBus.unregister(ApplicationModeEvent::class, applicationModeEventHandler)
		graphViewExecutionHandler.dispose()
		graphViewDisplayHandler.dispose()
		graphViewUsecaseExecutionHandler.dispose()
	}

	/**
	 * Updates the [DrawingView] in order to display whether the displayed [GraphView] is detached,
	 * i.e. whether it doesn't show accurate signal states due to shallow execution.
	 */
	fun updateDrawingViewEditability() {
		graphViewUI.drawingView.editable = isRoot
			&& !scheduler.isActive
			&& graphViewUI.isEditable
	}

	fun updateDetachedUI() {
		graphViewUI.drawingView.overlayColor = if (graphViewUI.isDetached && scheduler.isActive && !scheduler.isDeepExecution) {
			Themes.get<GraphTheme>().overlay
		} else {
			null
		}
	}

	private fun handle(event: SchedulerActivationStateEvent) {
		rootGraphProvider.invoke()?.apply {
			if (event.scheduler.isActive) {
				if (isRoot) {
					bind(repository, storableCreator)
				}
				graphViewsProvider.invoke().forEach { it.bind() }
				formNet(event.scheduler)
				executionInitialize(event.scheduler)
				executionStart(event.scheduler)
			} else {
				executionStopped(event.scheduler)
			}
		}
	}

	private fun handle(@Suppress("UNUSED_PARAMETER") event: ApplicationModeEvent) {
		graphViewUI.deselectAll()
		updateDrawingViewEditability()
		updateDetachedUI()
	}
}