package ch.scorpion.jabbah.graph.ui.graphpanel

import ch.scorpion.jabbah.app.*
import ch.scorpion.jabbah.base.IssueSeverity
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.ui.AbstractUIController
import ch.scorpion.jabbah.base.ui.UIView
import ch.scorpion.jabbah.draw.view.ActiveContentViewChangedEvent
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.Tool
import ch.scorpion.jabbah.edit.app.ComponentSnapAction
import ch.scorpion.jabbah.edit.app.GridSnapAction
import ch.scorpion.jabbah.edit.model.ComponentMessage
import ch.scorpion.jabbah.edit.model.ComponentMessageType
import ch.scorpion.jabbah.edit.model.QuadCurveTool
import ch.scorpion.jabbah.edit.model.curve.QuadCurveComponent
import ch.scorpion.jabbah.edit.model.polyline.PolylineComponent
import ch.scorpion.jabbah.edit.model.polyline.PolylineTool
import ch.scorpion.jabbah.edit.model.rectangle.EllipseComponent
import ch.scorpion.jabbah.edit.model.rectangle.RectangleComponent
import ch.scorpion.jabbah.edit.model.rectangle.RectangleTool
import ch.scorpion.jabbah.edit.model.text.EditModelTextModule
import ch.scorpion.jabbah.edit.model.text.TextTool
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.edit.ui.ComponentPropertyPanelController
import ch.scorpion.jabbah.execution.*
import ch.scorpion.jabbah.execution.issue.IssueCollectorEvent
import ch.scorpion.jabbah.execution.issue.IssuesView
import ch.scorpion.jabbah.execution.issue.IssuesViewController
import ch.scorpion.jabbah.execution.scheduler.ExecutionStoppedOnIssueEvent
import ch.scorpion.jabbah.execution.scheduler.SchedulerActivationStateEvent
import ch.scorpion.jabbah.graph.GraphApplicationContextHolder
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.app.ApplicationMode
import ch.scorpion.jabbah.graph.app.ApplicationModeBeginEvent
import ch.scorpion.jabbah.graph.app.ApplicationModeHolder
import ch.scorpion.jabbah.graph.app.ToggleApplicationModeAction
import ch.scorpion.jabbah.graph.library.ContainerLibraryElementRenamedEvent
import ch.scorpion.jabbah.graph.library.LibraryHolder
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.ui.GraphEditView
import ch.scorpion.jabbah.graph.ui.GraphEditViewController
import ch.scorpion.jabbah.graph.ui.GraphNavigationView
import ch.scorpion.jabbah.graph.ui.desktop.GraphDesktopView
import ch.scorpion.jabbah.graph.ui.desktop.GraphDesktopViewController
import ch.scorpion.jabbah.graph.ui.hierarchy.GraphHierarchyController
import ch.scorpion.jabbah.graph.ui.library.LibraryPanelController
import ch.scorpion.jabbah.graph.ui.logview.LogView
import ch.scorpion.jabbah.graph.ui.logview.LogViewController
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphElementViewWrapper
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.module.GraphViewModule

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
 * - Left side: A side bar with a library panel (Preview, Library tree, Properties) and
 * a properties panel for editing the properties of the selected [GraphElementView].
 * - Center: A [GraphDesktopView] with a [GraphEditView] and optionally multiple [GraphNavigationView]s
 * - Bottom: A side bar with a [LogView] and an [IssuesView]
 */
interface GraphPanelView : UIView {
	var issuesSummary: IssuesSummary?
	val graphEditView: GraphEditView
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

	val propertyPanelController = ComponentPropertyPanelController(editor, eventBus)
	val libraryPanelController = LibraryPanelController(applicationModeHolder, libraryHolder, eventBus)
	val editViewController = GraphEditViewController(editor, applicationModeHolder, applicationContextHolder, applicationDataHolder.data?.savable, eventBus)
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
	val pauseOrResumeAction = PauseOrResumeAction(applicationContextHolder.scheduler, eventBus)
	val executionDepthAction = ExecutionDepthAction(applicationContextHolder.scheduler, eventBus)
	val stopOnIssueAction = StopOnIssueAction(applicationContextHolder.scheduler, eventBus)
	val enableSoftBreakpointsAction = EnableSoftBreakpointsAction(applicationContextHolder.scheduler, eventBus)
	val simulationTimeStatusEnabledAction = SimulationTimeStatusEnabledAction(applicationContextHolder.scheduler, eventBus)

	val rectangleTool: Tool = RectangleTool(editor, factory = { RectangleComponent() }, adder = { GraphElementViewWrapper(it) })
	val ellipseTool: Tool = RectangleTool(editor, factory = { EllipseComponent() }, adder = { GraphElementViewWrapper(it) })
	val polylineTool: Tool = PolylineTool(editor, factory = { PolylineComponent() }, adder = { GraphElementViewWrapper(it) })
	val quadCurveTool: Tool = QuadCurveTool(editor, factory = { QuadCurveComponent() }, adder = { GraphElementViewWrapper(it) })
	val textTool: Tool = TextTool(editor, factory = { EditModelTextModule.textComponentFactory.create(TranslatableText("Text"))}, adder = { GraphElementViewWrapper(it) })

	private val applicationModeBeginHandler: EventHandler<ApplicationModeBeginEvent> = { handle(it) }
	private val applicationDataHandler: EventHandler<ApplicationDataEvent> = { handle(it) }
	private val applicationDataContentHandler: EventHandler<ApplicationDataContentEvent> = { handle(it) }
	private val schedulerActivationStateHandler: EventHandler<SchedulerActivationStateEvent> = { handle(it) }
	private val activeViewChangeHandler: EventHandler<ActiveContentViewChangedEvent> = { applicationModeHolder.updateEditorEditability() }
	private val issuesCollectorHandler:EventHandler<IssueCollectorEvent> = { handle(it) }
	private val executionStoppedOnIssueHandler: EventHandler<ExecutionStoppedOnIssueEvent> = { handle(it) }
	private val containerLibraryElementRenamedHandler: EventHandler<ContainerLibraryElementRenamedEvent> = { propertyPanelController.refresh() }

	private val rootGraphView: GraphView? get() = editViewController.editor.view.drawing as GraphView?

	init {
		eventBus.register(ApplicationModeBeginEvent::class, applicationModeBeginHandler)
		eventBus.register(ApplicationDataEvent::class, applicationDataHandler)
		eventBus.register(ApplicationDataContentEvent::class, applicationDataContentHandler)
		eventBus.register(SchedulerActivationStateEvent::class, schedulerActivationStateHandler)
		eventBus.register(ActiveContentViewChangedEvent::class, activeViewChangeHandler)
		eventBus.register(IssueCollectorEvent::class, issuesCollectorHandler)
		eventBus.register(ExecutionStoppedOnIssueEvent::class, executionStoppedOnIssueHandler)
		eventBus.register(ContainerLibraryElementRenamedEvent::class, containerLibraryElementRenamedHandler)
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

	private fun handle(@Suppress("UNUSED_PARAMETER") event: ExecutionStoppedOnIssueEvent) {
		if (event.scheduler === applicationContextHolder.scheduler) {
			eventBus.post(ComponentMessage(
				type = ComponentMessageType.Error,
				source = null,
				messageKey = "execution.scheduler.pausedDueToIssue.msg"))
		}
	}

	private fun handle(event: ApplicationDataEvent) {
		stopSimulationWhenClosingApplicationData(event.newData)
		issuesViewController.clearIssues()

		val editable = event.newData?.savable?.editable ?: false

		if (event.newData?.content == null) {
			setGraphViewApplicationData(null, editable)
		} else if (event.newData?.content is MetaGraph) {
			setGraphViewApplicationData((event.newData?.content as MetaGraph?)?.graph?.graphView, editable)
			LOG.trace("Set MetaGraph with ID ${(event.newData?.content as MetaGraph?)?.hashCode()} for editing")
		}
	}

	private fun stopSimulationWhenClosingApplicationData(data: ApplicationData?) {
		if (data == null && applicationModeHolder.currentMode.isExecute()) {
			applicationModeHolder.setMode(ApplicationMode.EDIT)
		}
	}

	fun setGraphViewApplicationData(graphView: GraphView?, editable: Boolean) {
		isSavableEditable = editable
		if (graphView == null) {
			// Set empty drawing to avoid flickering (i.e. showing the old drawing) when
			// the subsequent drawing gets displayed
			editor.view.setDrawing(GraphViewModule.graphViewFactory(null) as Drawing<Component>, applyDefaultZoomStrategy = false)
			graphHierarchyController.setRootGraphView(null)
			desktopController.closeAll()
		} else if (rootGraphView != graphView) {
			desktopController.show(view.graphEditView)
			System.invokeLater {
				// This will apply the Zoom strategy, which requires that the main Swing UI has already been laid out
				setRootGraphView(graphView, applyZoomStrategy = true)
			}
		} else {
			closeRootGraphView()
		}
	}

	private fun closeRootGraphView() {
		setRootGraphView(null, false)
	}

	private fun handle(event: ApplicationDataContentEvent) {
		setApplicationDataContent((event.data.content as MetaGraph?)?.graph?.graphView)
	}

	/**
	 * This is primarily called when the states is replayed from undoable history,
	 * and the undoable commands are replayed immediately after the the new [GraphView]
	 * has been set, which is why invoking this later would not work.
	 */
	private fun setApplicationDataContent(graphView: GraphView?) {
		if (rootGraphView != graphView) {
			setRootGraphView(graphView, applyZoomStrategy = false)
		}
	}

	private fun setRootGraphView(graphView: GraphView?, applyZoomStrategy: Boolean) {
		val oldValue = rootGraphView
		graphView?.let {
			editViewController.setGraphView(it, isSavableEditable, applyZoomStrategy)
			it.snapper = editor.view.grid
		}
		graphHierarchyController.setRootGraphView(graphView)
		eventBus.post(EditedGraphViewEvent(oldValue, graphView))
		applicationModeHolder.updateEditorEditability()
	}
}