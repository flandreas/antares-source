package ch.scorpion.jabbah.graph.ui.graphpanel

import ch.scorpion.jabbah.app.ApplicationData
import ch.scorpion.jabbah.app.ApplicationDataContentEvent
import ch.scorpion.jabbah.app.ApplicationDataEvent
import ch.scorpion.jabbah.app.Savable
import ch.scorpion.jabbah.base.*
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.event.PropertyChangeEvent
import ch.scorpion.jabbah.base.event.PropertyChangeListener
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.ui.AbstractUIController
import ch.scorpion.jabbah.base.ui.UIView
import ch.scorpion.jabbah.draw.view.ActiveViewChangedEvent
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.draw.view.ViewManager
import ch.scorpion.jabbah.edit.DrawingView
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
import ch.scorpion.jabbah.execution.issue.IssueCollectorEvent
import ch.scorpion.jabbah.execution.issue.IssueSeverity
import ch.scorpion.jabbah.execution.issue.IssuesView
import ch.scorpion.jabbah.execution.issue.IssuesViewController
import ch.scorpion.jabbah.execution.module.ExecutionModule
import ch.scorpion.jabbah.execution.scheduler.ExecutionStoppedOnIssueEvent
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import ch.scorpion.jabbah.execution.scheduler.SchedulerActivationStateEvent
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.app.*
import ch.scorpion.jabbah.graph.ui.*
import ch.scorpion.jabbah.graph.ui.logview.LogView
import ch.scorpion.jabbah.graph.ui.logview.LogViewController
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphElementViewWrapper
import ch.scorpion.jabbah.graph.view.GraphView

/** Posted on [EventBus] when the currently (one and only) edited root [GraphView] changes. */
class EditedGraphViewEvent(
	val oldGraphView: GraphView?,
	val newGraphView: GraphView?
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
	var maxIssueSeverity: IssueSeverity?
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
	private val viewManager: ViewManager = DrawViewModule.viewManager,
	private val scheduler: Scheduler =ExecutionModule.scheduler,
	private val eventBus: EventBus = BaseModule.eventBus,
	private val applicationModeHolder: ApplicationModeHolder = ApplicationModeHolderImpl(editor, viewManager, scheduler, eventBus)
) : AbstractUIController<GraphPanelView>(), ApplicationModeHolder by applicationModeHolder {

	val editViewController = GraphEditViewController(editor.view as DrawingView<GraphView>, eventBus)
	val desktopController = GraphDesktopViewController(eventBus = eventBus)
	val issuesViewController = IssuesViewController(eventBus = eventBus)
	val logViewController = LogViewController(eventBus)

	/**
	 * Captures whether the [Savable] designated the current [GraphView] as 'editable'.
	 * Only updated when received [ApplicationDataEvent]'s are processed.
	 */
	private var isSavableEditable: Boolean = true

	val gridSnapAction = GridSnapAction(editor)
	val componentSnapAction = ComponentSnapAction(editor)
	val toggleApplicationModeAction = ToggleApplicationModeAction(eventBus)

	val rectangleTool: Tool = RectangleTool(editor, factory = { RectangleComponent() }, adder = { GraphElementViewWrapper(it) })
	val ellipseTool: Tool = RectangleTool(editor, factory = { EllipseComponent() }, adder = { GraphElementViewWrapper(it) })
	val polylineTool: Tool = PolylineTool(editor, factory = { PolylineComponent() }, adder = { GraphElementViewWrapper(it) })
	val quadCurveTool: Tool = QuadCurveTool(editor, factory = { QuadCurveComponent() }, adder = { GraphElementViewWrapper(it) })
	val textTool: Tool = TextTool(editor, factory = { EditModelTextModule.textComponentFactory.create(TranslatableText("Text"))}, adder = { GraphElementViewWrapper(it) })

	private val applicationModeBeginHandler: EventHandler<ApplicationModeBeginEvent> = { handle(it) }
	private val applicationDataHandler: EventHandler<ApplicationDataEvent> = { handle(it) }
	private val applicationDataContentHandler: EventHandler<ApplicationDataContentEvent> = { handle(it) }
	private val schedulerActivationStateHandler: EventHandler<SchedulerActivationStateEvent> = { handle(it) }
	private val activeViewChangeHandler: EventHandler<ActiveViewChangedEvent> = { updateEditorEditability() }
	private val issuesCollectorHandler:EventHandler<IssueCollectorEvent> = { handle(it) }
	private val executionStoppedOnIssueHandler: EventHandler<ExecutionStoppedOnIssueEvent> = { handle(it) }
	private val editorViewListener = EditorViewListener()

	private val rootGraphView: GraphView? get() = editViewController.drawingView.drawing

	init {
		editor.view.addPropertyChangeListener(editorViewListener)
		eventBus.register(ApplicationModeBeginEvent::class, applicationModeBeginHandler)
		eventBus.register(ApplicationDataEvent::class, applicationDataHandler)
		eventBus.register(ApplicationDataContentEvent::class, applicationDataContentHandler)
		eventBus.register(SchedulerActivationStateEvent::class, schedulerActivationStateHandler)
		eventBus.register(ActiveViewChangedEvent::class, activeViewChangeHandler)
		eventBus.register(IssueCollectorEvent::class, issuesCollectorHandler)
		eventBus.register(ExecutionStoppedOnIssueEvent::class, executionStoppedOnIssueHandler)
	}

	/** ---- [AbstractUIController] */

	override fun dispose() {
		super.dispose()

		editor.view.removePropertyChangeListener(editorViewListener)
		eventBus.unregister(applicationModeBeginHandler)
		eventBus.unregister(applicationDataHandler)
		eventBus.unregister(applicationDataContentHandler)
		eventBus.unregister(schedulerActivationStateHandler)
		eventBus.unregister(activeViewChangeHandler)
		eventBus.unregister(issuesCollectorHandler)
		eventBus.unregister(executionStoppedOnIssueHandler)

		editViewController.dispose()
		desktopController.dispose()
		issuesViewController.dispose()
		logViewController.dispose()
	}

	/** ---- [GraphPanelViewController] */

	private fun handle(event: SchedulerActivationStateEvent) {
		if (event.scheduler === scheduler && !event.scheduler.isActive) {
			setMode(ApplicationMode.EDIT)
		}
	}

	private fun handle(event: IssueCollectorEvent) {
		view.maxIssueSeverity = event.issueCollector.maximumSeverity
	}

	private fun handle(event: ApplicationModeBeginEvent) {
		issuesViewController.clearIssues()
	}

	private fun handle(@Suppress("UNUSED_PARAMETER") event: ExecutionStoppedOnIssueEvent) {
		eventBus.post(ComponentMessage(
			type = ComponentMessageType.Error,
			source = null,
			messageKey = "execution.scheduler.stoppedDueToIssue.msg"))
	}

	private fun handle(event: ApplicationDataEvent) {
		isSavableEditable = event.newData?.savable?.editable ?: false
		setApplicationData((event.newData?.content as MetaGraph?)?.graph?.graphView)
	}

	private fun setApplicationData(graphView: GraphView?) {
		if (graphView == null) {
			desktopController.closeAll()
		} else if (rootGraphView != graphView) {
			desktopController.showMainOnly()
			System.invokeLater {
				// This will apply the Zoom strategy, which requires that the main Swing UI has already been laid out
				setRootGraphView(graphView, applyZoomStrategy = true)
			}
		}
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
		eventBus.post(EditedGraphViewEvent(oldValue, graphView))
		updateEditorEditability()
	}

	private inner class EditorViewListener : PropertyChangeListener<Any> {
		override fun propertyChanged(e: PropertyChangeEvent<Any>) {
			if (e.name == DrawingView.PROP_EDITABLE) {
				updateEditorEditability()
			}
		}
	}
}