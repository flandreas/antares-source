package io.antarescircuit.jabbah.graph.ui.graphpanel

import io.antarescircuit.jabbah.app.*
import io.antarescircuit.jabbah.app.properties.ApplicationDataPropertyPanelController
import io.antarescircuit.jabbah.base.IssueSeverity
import io.antarescircuit.jabbah.base.System
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.event.EventHandler
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.ui.AbstractUIController
import io.antarescircuit.jabbah.base.ui.UIView
import io.antarescircuit.jabbah.draw.view.ActiveContentViewChangedEvent
import io.antarescircuit.jabbah.draw.view.FocusDrawablePlayer
import io.antarescircuit.jabbah.edit.*
import io.antarescircuit.jabbah.edit.app.ComponentSnapAction
import io.antarescircuit.jabbah.edit.app.GridSnapAction
import io.antarescircuit.jabbah.edit.model.ComponentMessage
import io.antarescircuit.jabbah.edit.model.ComponentMessageType
import io.antarescircuit.jabbah.edit.model.curve.CubicCurveTool
import io.antarescircuit.jabbah.edit.model.curve.CubicCurveComponent
import io.antarescircuit.jabbah.edit.model.curve.QuadCurveTool
import io.antarescircuit.jabbah.edit.model.curve.QuadCurveComponent
import io.antarescircuit.jabbah.edit.model.polyline.PolylineComponent
import io.antarescircuit.jabbah.edit.model.polyline.PolylineTool
import io.antarescircuit.jabbah.edit.model.rectangle.EllipseComponent
import io.antarescircuit.jabbah.edit.model.rectangle.RectangleComponent
import io.antarescircuit.jabbah.edit.model.rectangle.RectangleTool
import io.antarescircuit.jabbah.edit.model.text.EditModelTextModule
import io.antarescircuit.jabbah.edit.model.text.TextTool
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.edit.model.text.description.NameChangedEvent
import io.antarescircuit.jabbah.edit.module.EditModule
import io.antarescircuit.jabbah.execution.*
import io.antarescircuit.jabbah.execution.issue.IssueCollectorEvent
import io.antarescircuit.jabbah.execution.issue.IssuesView
import io.antarescircuit.jabbah.execution.issue.IssuesViewController
import io.antarescircuit.jabbah.execution.scheduler.ExecutionStoppedOnIssueEvent
import io.antarescircuit.jabbah.execution.scheduler.SchedulerActivationStateEvent
import io.antarescircuit.jabbah.graph.GraphApplicationContextHolder
import io.antarescircuit.jabbah.graph.MetaGraph
import io.antarescircuit.jabbah.graph.app.ApplicationMode
import io.antarescircuit.jabbah.graph.app.ApplicationModeBeginEvent
import io.antarescircuit.jabbah.graph.app.ApplicationModeHolder
import io.antarescircuit.jabbah.graph.app.ToggleApplicationModeAction
import io.antarescircuit.jabbah.graph.library.ContainerLibraryElement
import io.antarescircuit.jabbah.graph.library.LibraryHolder
import io.antarescircuit.jabbah.graph.library.LibraryModule
import io.antarescircuit.jabbah.graph.model.vertice.EnableInteractivePropagationDelayAction
import io.antarescircuit.jabbah.graph.ui.GraphEditView
import io.antarescircuit.jabbah.graph.ui.GraphEditViewController
import io.antarescircuit.jabbah.graph.ui.GraphNavigationView
import io.antarescircuit.jabbah.graph.ui.desktop.GraphDesktopView
import io.antarescircuit.jabbah.graph.ui.desktop.GraphDesktopViewController
import io.antarescircuit.jabbah.graph.ui.hierarchy.GraphHierarchyController
import io.antarescircuit.jabbah.graph.ui.library.LibraryPanelController
import io.antarescircuit.jabbah.graph.ui.logview.LogView
import io.antarescircuit.jabbah.graph.ui.logview.LogViewController
import io.antarescircuit.jabbah.graph.view.GraphElementView
import io.antarescircuit.jabbah.graph.view.GraphElementViewWrapper
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule

/** Posted on [EventBus] when the currently (one and only) edited root [GraphView] changes. */
class EditedGraphViewEvent(
	val oldGraphView: GraphView?,
	val newGraphView: GraphView?
)

data class IssuesSummary(
	val maxIssueSeverity: IssueSeverity?,
	val issuesCount: Int
)

/**
 * A [UIView] for editing and executing a root [GraphView].
 *
 * Consists of the following parts:
 * - Left side: A sidebar with a library panel (Preview, Library tree, Properties) and
 * a properties panel for editing the properties of the selected [GraphElementView].
 * - Center: A [GraphDesktopView] with a [GraphEditView] and optionally multiple [GraphNavigationView]s
 * - Bottom: A sidebar with a [LogView] and an [IssuesView]
 */
interface GraphPanelView : UIView {
	var issuesSummary: IssuesSummary?
	val graphEditView: GraphEditView

	fun updateDynamicToolbar()
}

/**
 * Controls a [GraphPanelView] and holds the current [ApplicationMode], allowing the user
 * to switch between edit mode and execution mode.
 *
 * Listens for [ApplicationDataEvent]s and extracts the [GraphView] from its [MetaGraph] to
 * be displayed as the main [GraphView]. Posts a [EditedGraphViewEvent] whenever the [GraphView]
 * changes.
 *
 * Checks the root [GraphView] for design errors when execution is started and displays
 * a [ComponentMessage] if any are found.
 *
 * Controls editability of the current [GraphView] depending on the [ApplicationMode]
 * (no editing while execution) and the editability of the current [ApplicationData]'s [Savable].
 */
class GraphPanelViewController(
	drawingView: DrawingView<GraphElementView<*>, GraphView>,
	val editor: Editor,
	applicationDataHolder: ApplicationDataHolder,
	val applicationContextHolder: GraphApplicationContextHolder,
	val applicationModeHolder: ApplicationModeHolder,
	private val eventBus: EventBus = BaseModule.eventBus,
	libraryHolder: LibraryHolder = LibraryModule.libraryHolder,
) : AbstractUIController<GraphPanelView>() {

	companion object {
		private val LOG by logger(GraphPanelViewController::class)
	}

	val propertyPanelController = ApplicationDataPropertyPanelController(editor, eventBus, currentEditorEventFilter = { e -> e.editor.name == editor.name })
	val libraryPanelController = LibraryPanelController(applicationModeHolder, editor, libraryHolder, eventBus)
	val editViewController = GraphEditViewController(
		drawingView,
		editor,
		applicationDataHolder,
		applicationModeHolder,
		applicationContextHolder,
		applicationDataHolder.data?.savable,
		eventBus)
	val desktopController = GraphDesktopViewController(applicationContextHolder, eventBus = eventBus)
	val issuesViewController = IssuesViewController(eventBus = eventBus)
	val logViewController = LogViewController(applicationContextHolder, eventBus)
	val graphHierarchyController = GraphHierarchyController()

	/**
	 * Captures whether the [Savable] designated the current [GraphView] as 'editable'.
	 * Only updated when received [ApplicationDataEvent]'s are processed.
	 */
	private var isSavableEditable: Boolean = true

	val gridSnapAction = GridSnapAction(editor)
	val componentSnapAction = ComponentSnapAction(editor)

	val toggleApplicationModeAction = ToggleApplicationModeAction(applicationDataHolder, applicationModeHolder, eventBus)
	val singleStepModeAction = SingleStepModeAction(applicationContextHolder.scheduler, eventBus)
	val pauseOrResumeAction = PauseOrResumeActionImpl(applicationContextHolder.scheduler, eventBus)
	val executionDepthAction = ExecutionDepthAction(applicationContextHolder.scheduler, eventBus)
	val stopOnIssueAction = StopOnIssueAction(applicationContextHolder.scheduler, eventBus)
	val enableSoftBreakpointsAction = EnableSoftBreakpointsAction(applicationContextHolder.scheduler, eventBus)
	val simulationTimeStatusEnabledAction = SimulationTimeStatusEnabledAction(applicationContextHolder.scheduler, eventBus)
	val enableInteractivePropagationDelayAction = EnableInteractivePropagationDelayAction()

	val rectangleTool: Tool = RectangleTool(editor, factory = { RectangleComponent() }, adder = { GraphElementViewWrapper(it) })
	val ellipseTool: Tool = RectangleTool(editor, factory = { EllipseComponent() }, adder = { GraphElementViewWrapper(it) })
	val polylineTool: Tool = PolylineTool(editor, factory = { PolylineComponent() }, adder = { GraphElementViewWrapper(it) })
	val quadCurveTool: Tool = QuadCurveTool(editor, factory = { QuadCurveComponent() }, adder = { GraphElementViewWrapper(it) })
	val cubicCurveTool: Tool = CubicCurveTool(editor, factory = { CubicCurveComponent() }, adder = { GraphElementViewWrapper(it) })
	val textTool: Tool = TextTool(editor, factory = { EditModelTextModule.textComponentFactory.create(TranslatableText("Text"))}, adder = { GraphElementViewWrapper(it) })

	private val applicationModeBeginHandler: EventHandler<ApplicationModeBeginEvent> = { handle(it) }
	private val applicationDataHandler: EventHandler<ApplicationDataEvent> = { handle(it) }
	private val applicationDataContentHandler: EventHandler<ApplicationDataContentEvent> = { handle(it) }
	private val schedulerActivationStateHandler: EventHandler<SchedulerActivationStateEvent> = { handle(it) }
	private val activeViewChangeHandler: EventHandler<ActiveContentViewChangedEvent> = { applicationModeHolder.updateEditorEditability() }
	private val issuesCollectorHandler:EventHandler<IssueCollectorEvent> = { handle(it) }
	private val executionStoppedOnIssueHandler: EventHandler<ExecutionStoppedOnIssueEvent> = { handle(it) }
	private val containerLibraryElementRenamedHandler: EventHandler<NameChangedEvent> = {
		if (it.owner is ContainerLibraryElement) {
			propertyPanelController.refresh()
		}
	}

	private val rootGraphView: GraphView? get() = editViewController.editor.view.drawing as GraphView?

	init {
		eventBus.register(ApplicationModeBeginEvent::class, applicationModeBeginHandler)
		eventBus.register(ApplicationDataEvent::class, applicationDataHandler)
		eventBus.register(ApplicationDataContentEvent::class, applicationDataContentHandler)
		eventBus.register(SchedulerActivationStateEvent::class, schedulerActivationStateHandler)
		eventBus.register(ActiveContentViewChangedEvent::class, activeViewChangeHandler)
		eventBus.register(IssueCollectorEvent::class, issuesCollectorHandler)
		eventBus.register(ExecutionStoppedOnIssueEvent::class, executionStoppedOnIssueHandler)
		eventBus.register(NameChangedEvent::class, containerLibraryElementRenamedHandler)

		desktopController.addPropertyChangeListener { _ -> view.updateDynamicToolbar() }
	}

	/** ---- [AbstractUIController] */

	override fun dispose() {
		super.dispose()

		applicationModeHolder.dispose()

		eventBus.unregister(applicationModeBeginHandler)
		eventBus.unregister(applicationDataHandler)
		eventBus.unregister(applicationDataContentHandler)
		eventBus.unregister(schedulerActivationStateHandler)
		eventBus.unregister(activeViewChangeHandler)
		eventBus.unregister(issuesCollectorHandler)
		eventBus.unregister(executionStoppedOnIssueHandler)
		eventBus.unregister(containerLibraryElementRenamedHandler)

		propertyPanelController.dispose()
		libraryPanelController.dispose()
		editViewController.dispose()
		desktopController.dispose()
		issuesViewController.dispose()
		logViewController.dispose()
		graphHierarchyController.dispose()

		executionDepthAction.dispose()
	}

	/** ---- [GraphPanelViewController] */

	private fun handle(event: SchedulerActivationStateEvent) {
		if (event.scheduler === applicationContextHolder.scheduler) {
			if (!event.scheduler.isActive) {
				applicationModeHolder.setMode(ApplicationMode.EDIT)
			}
			EditModule.commandManager.active = !event.scheduler.isActive
		}
	}

	private fun handle(event: IssueCollectorEvent) {
		view.issuesSummary = IssuesSummary(
			event.issueCollector.maximumSeverity,
			event.issueCollector.size)
	}

	private fun handle(@Suppress("UNUSED_PARAMETER") event: ApplicationModeBeginEvent) {
		issuesViewController.clearIssues()
	}

	private fun handle(event: ExecutionStoppedOnIssueEvent) {
		if (event.scheduler === applicationContextHolder.scheduler) {
			eventBus.post(ComponentMessage(
				type = ComponentMessageType.Error,
				source = null,
				messageKey = "execution.scheduler.pausedDueToIssue.msg"))
		}
	}

	private fun handle(event: ApplicationDataEvent) {
		stopSimulationWhenClosingApplicationData(event.newData)

		val editable = event.newData?.savable?.editable ?: false

		if (event.newData?.content == null) {
			setGraphViewApplicationData(null, editable, null)
		} else if (event.newData?.content is MetaGraph) {
			setGraphViewApplicationData(
				(event.newData?.content as MetaGraph?)?.graph?.graphView,
				editable,
				event.newData?.focusItem as Int?)
			LOG.trace("Set MetaGraph with ID ${(event.newData?.content as MetaGraph?)?.hashCode()} for editing")
		}
	}

	private fun stopSimulationWhenClosingApplicationData(data: ApplicationData?) {
		if (data == null && applicationModeHolder.currentMode.isExecute()) {
			applicationModeHolder.setMode(ApplicationMode.EDIT)
		}
	}

	private fun setGraphViewApplicationData(graphView: GraphView?, editable: Boolean, focusVerticeViewId: Int?) {
		eventBus.post(CurrentEditorEvent(editor))
		isSavableEditable = editable
		if (graphView == null) {
			// Set empty drawing to avoid flickering (i.e. showing the old drawing) when
			// the subsequent drawing gets displayed
			@Suppress("UNCHECKED_CAST")
			editor.view.setDrawing(GraphViewModule.graphViewFactory.create(null) as Drawing<Component>, applyDefaultZoomStrategy = false)
			graphHierarchyController.setRootGraphView(null)
			desktopController.closeAll()
		} else if (rootGraphView != graphView) {
			desktopController.show(view.graphEditView)
			System.invokeLater {
				// This will apply the Zoom strategy, which requires that the main Swing UI has already been laid out
				setRootGraphView(graphView, applyZoomStrategy = true, focusVerticeViewId)
			}
		}
	}

	private fun handle(event: ApplicationDataContentEvent) {
		if (event.data.content is MetaGraph) {
			setApplicationDataContent((event.data.content as MetaGraph?)?.graph?.graphView)
		}
	}

	/**
	 * This is primarily called when the state is replayed from undoable history,
	 * and the undoable commands are replayed immediately after the new [GraphView]
	 * has been set, which is why invoking this later would not work.
	 */
	private fun setApplicationDataContent(graphView: GraphView?) {
		if (rootGraphView != graphView) {
			setRootGraphView(graphView, applyZoomStrategy = false, null)
		}
	}

	private fun setRootGraphView(graphView: GraphView?, applyZoomStrategy: Boolean, focusVerticeViewId: Int?) {
		val oldValue = rootGraphView
		graphView?.let {
			editViewController.setGraphView(it, isSavableEditable, applyZoomStrategy)
			it.snapper = editor.view.grid
			focusVerticeViewId?.let { id -> focusVerticeView(id) }
		}
		graphHierarchyController.setRootGraphView(graphView)
		eventBus.post(EditedGraphViewEvent(oldValue, graphView))
		applicationModeHolder.updateEditorEditability()
	}

	private fun focusVerticeView(id: Int) {
		editor.view.drawing.getWithId(id)?.let {
			editor.view.selectionManager.deselectAll()
			editor.view.selectionManager.select(it)
			FocusDrawablePlayer.playFocus(it, editor.view)
		}
	}
}