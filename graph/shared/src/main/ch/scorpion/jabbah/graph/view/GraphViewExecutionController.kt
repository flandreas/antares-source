package ch.scorpion.jabbah.graph.view

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.execution.module.ExecutionModule
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import ch.scorpion.jabbah.execution.scheduler.SchedulerActivationStateEvent
import ch.scorpion.jabbah.graph.MetaGraphRepository
import ch.scorpion.jabbah.graph.app.ApplicationMode
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
import ch.scorpion.jabbah.graph.ui.GraphViewDisplayHandler
import ch.scorpion.jabbah.graph.ui.GraphViewExecutionHandler
import ch.scorpion.jabbah.graph.ui.GraphViewUsecaseExecutionHandler
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
	drawingView: DrawingView<GraphView>,
	private val isRoot: Boolean,
	private val rootGraphProvider: () -> Graph,
	private val graphViewsProvider: () -> Collection<GraphView>,
	scheduler: Scheduler = ExecutionModule.scheduler,
	private val repository: MetaGraphRepository = GraphModelModule.metaGraphRepository,
	private val storableCreator: StorableCreator = IOModule.storableCreator,
	private val eventBus: EventBus = BaseModule.eventBus
) {

	private val initialMode: ApplicationMode = if (scheduler.isActive) ApplicationMode.EXECUTE else ApplicationMode.EDIT

	private val schedulerActivationStateHandler: (SchedulerActivationStateEvent) -> Unit = { handle(it) }

	/** Forwards input events to the [GraphView] while executing.*/
	private val graphViewExecutionHandler = GraphViewExecutionHandler(drawingView, scheduler, eventBus, initialMode)

	/** Forwards input events to the [GraphView] while displaying (i.e. NOT executing) and NOT being editable.*/
	private val graphViewDisplayHandler = GraphViewDisplayHandler(drawingView, scheduler, eventBus)

	/** Forwards input events to the [GraphView] while a [Usecase] is executed.*/
	private val graphViewUsecaseExecutionHandler = GraphViewUsecaseExecutionHandler(drawingView, scheduler, eventBus, initialMode)

	init {
		eventBus.register(SchedulerActivationStateEvent::class, schedulerActivationStateHandler)
	}

	fun dispose() {
		eventBus.unregister(SchedulerActivationStateEvent::class, schedulerActivationStateHandler)
		graphViewExecutionHandler.dispose()
		graphViewDisplayHandler.dispose()
		graphViewUsecaseExecutionHandler.dispose()
	}

	private fun handle(event: SchedulerActivationStateEvent) {
		val rootGraph = rootGraphProvider.invoke()
		if (event.scheduler.isActive) {
			if (isRoot) {
				rootGraph.bind(repository, storableCreator)
			}
			graphViewsProvider.invoke().forEach { it.bind() }
			rootGraph.executionStarted(event.scheduler)
		} else {
			rootGraph.executionStopped(event.scheduler)
		}
	}
}