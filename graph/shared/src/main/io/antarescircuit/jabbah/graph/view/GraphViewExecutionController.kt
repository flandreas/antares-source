package io.antarescircuit.jabbah.graph.view

import io.antarescircuit.jabbah.base.PROP_BEGINNER_HELP_TOOLTIP
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.draw.ViewDecorator
import io.antarescircuit.jabbah.draw.style.Themes
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.DrawingViewContent
import io.antarescircuit.jabbah.edit.model.text.HorizontalAlignment
import io.antarescircuit.jabbah.edit.model.text.Label
import io.antarescircuit.jabbah.edit.model.text.VerticalAlignment
import io.antarescircuit.jabbah.execution.scheduler.SchedulerActivationStateEvent
import io.antarescircuit.jabbah.graph.GraphApplicationContextHolder
import io.antarescircuit.jabbah.graph.MetaGraphRepository
import io.antarescircuit.jabbah.graph.app.ApplicationMode
import io.antarescircuit.jabbah.graph.app.ApplicationModeEvent
import io.antarescircuit.jabbah.graph.library.LibraryModule
import io.antarescircuit.jabbah.graph.model.Graph
import io.antarescircuit.jabbah.graph.model.module.GraphModelModule
import io.antarescircuit.jabbah.graph.model.nonvolatile.NonVolatileDeterminator
import io.antarescircuit.jabbah.graph.model.nonvolatile.NonVolatileStorable
import io.antarescircuit.jabbah.graph.ui.GraphViewDisplayHandler
import io.antarescircuit.jabbah.graph.ui.GraphViewExecutionHandler
import io.antarescircuit.jabbah.graph.ui.GraphViewUI
import io.antarescircuit.jabbah.graph.ui.GraphViewUsecaseExecutionHandler
import io.antarescircuit.jabbah.graph.view.style.GraphTheme

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
	private val repository: MetaGraphRepository = LibraryModule.libraryHolder,
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

	/**
	 * Remove all animation highlighting artifacts from the animation container of the
	 * specified [DrawingViewContent]. Called after execution has been stopped.
	 */
	fun cleanup(content: DrawingViewContent<*,*>) {
		actorListener.executionAnimator.cleanup(content)
	}

	fun updateDrawingViewEditability() {
		graphViewUI.drawingView.editable =
			isRoot
			&& !applicationContextHolder.scheduler.isActive
			&& graphViewUI.isEditable
	}

	private val isDetached: Boolean get() = graphViewUI.isDetached
			&& applicationContextHolder.scheduler.isActive
			&& (!applicationContextHolder.scheduler.isDeepExecution || graphViewUI.drawingView.drawing.graph!!.purelyScripted)

	/**
	 * Updates the [DrawingView] in order to display whether the displayed [GraphView] is detached,
	 * i.e. whether it doesn't show accurate signal states due to shallow execution.
	 */
	fun updateDetachedUI() {
		if (isDetached) {
			graphViewUI.drawingView.overlayColor = Themes.get<GraphTheme>().overlay
			if (BaseModule.properties.getBoolean(PROP_BEGINNER_HELP_TOOLTIP)) {
				graphViewUI.drawingView.decorator.topCentered = Label(
					Translations.getString("graph.sim.scripted.shallow.txt"),
					ViewDecorator.FONT,
					ViewDecorator.TEXT_COLOR,
					horizontalAlignment = HorizontalAlignment.LEFT,
					verticalAlignment = VerticalAlignment.TOP
				)
			}
		} else {
			graphViewUI.drawingView.overlayColor = null
			graphViewUI.drawingView.decorator.topCentered = null
		}
		graphViewUI.drawingView.drawing.handleDetached(isDetached)
	}

	private fun handle(event: SchedulerActivationStateEvent) {
		if (event.scheduler === applicationContextHolder.scheduler) {
			rootGraphProvider.invoke()?.apply {
				if (event.scheduler.isActive) {
					if (isRoot) {
						bind(event.scheduler.isDeepExecution, repository)
						startupTime?.let {
							event.scheduler.softBreakpointsArmTime = it
						}
					}
					graphViewsProvider.invoke().forEach {
						it.bind(event.scheduler.isDeepExecution)
					}
					if (isRoot) {
						formNet(event.scheduler)
						executionInitialize(event.scheduler, GraphModelModule.nonVolatileService.load(uuid))
						executionStart(event.scheduler, null)
					}
					graphViewsProvider.invoke().forEach {
						it.executionStart(event.scheduler)
					}
					graphViewsProvider.invoke().forEach {
						it.executionStartDone(event.scheduler)
					}
				} else {
					graphViewsProvider.invoke().forEach {
						it.executionStop(event.scheduler)
					}
					if (isRoot && NonVolatileDeterminator().hasNonVolatileData(this)) {
						val nonVolatileStorable = NonVolatileStorable()
						executionStopped(event.scheduler, nonVolatileStorable)
						if (nonVolatileStorable.hasChildren) {
							GraphModelModule.nonVolatileService.store(uuid, nonVolatileStorable)
						} else {
							GraphModelModule.nonVolatileService.delete(uuid)
						}
					} else {
						executionStopped(event.scheduler)
					}
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