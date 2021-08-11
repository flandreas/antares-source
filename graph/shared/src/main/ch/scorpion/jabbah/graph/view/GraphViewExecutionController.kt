package ch.scorpion.jabbah.graph.view

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.execution.scheduler.SchedulerActivationStateEvent
import ch.scorpion.jabbah.graph.GraphApplicationContextHolder
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
 * Used by objects that hold [GraphView]s to control starting and stopping
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
	private val applicationContextHolder: GraphApplicationContextHolder,
	private val repository: MetaGraphRepository = GraphModelModule.metaGraphRepository,
	private val storableCreator: StorableCreator = IOModule.storableCreator,
	private val eventBus: EventBus = BaseModule.eventBus
) {

	private val schedulerActivationStateHandler: (SchedulerActivationStateEvent) -> Unit = { handle(it) }

	private val applicationModeEventHandler: (ApplicationModeEvent) -> Unit = { handle(it) }

	/** Forwards input events to the [GraphView] while executing.*/
	private val graphViewExecutionHandler = GraphViewExecutionHandler(graphViewUI.drawingView, applicationContextHolder, eventBus)

	/** Forwards input events to the [GraphView] while displaying (i.e. NOT executing) and NOT being editable.*/
	private val graphViewDisplayHandler = GraphViewDisplayHandler(graphViewUI.drawingView, applicationContextHolder, eventBus)

	/** Forwards input events to the [GraphView] while a [Usecase] is executed.*/
	private val graphViewUsecaseExecutionHandler = GraphViewUsecaseExecutionHandler(graphViewUI.drawingView, applicationContextHolder, eventBus)

	private val actorListener = GraphViewActorListener(graphViewUI.drawingView, applicationContextHolder, eventBus = eventBus)

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
		actorListener.dispose()
	}

	fun updateDrawingViewEditability() {
		graphViewUI.drawingView.editable = isRoot
			&& !applicationContextHolder.scheduler.isActive
			&& graphViewUI.isEditable
	}

	/**
	 * Updates the [DrawingView] in order to display whether the displayed [GraphView] is detached,
	 * i.e. whether it doesn't show accurate signal states due to shallow execution.
	 */
	fun updateDetachedUI() {
		graphViewUI.drawingView.overlayColor = if (graphViewUI.isDetached
			&& applicationContextHolder.scheduler.isActive
			&& (!applicationContextHolder.scheduler.isDeepExecution || graphViewUI.drawingView.drawing.graph!!.purelyScripted)
		) {
			Themes.get<GraphTheme>().overlay
		} else {
			null
		}
	}

	private fun handle(event: SchedulerActivationStateEvent) {
		if (event.scheduler === applicationContextHolder.scheduler) {
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
	}

	private fun handle(@Suppress("UNUSED_PARAMETER") event: ApplicationModeEvent) {
		if (event.source === applicationContextHolder.applicationModeHolder) {
			graphViewUI.deselectAll()
			updateDrawingViewEditability()
			updateDetachedUI()
		}
	}
}