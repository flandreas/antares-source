package ch.scorpion.jabbah.graph.ui.graphviewer

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.CloseViewRequest
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.ui.ExecutionToolbarSwing
import ch.scorpion.jabbah.graph.ui.GraphNavigationViewSwing
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Frame
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JFrame
import javax.swing.SwingUtilities

class GraphViewerFrameSwing(
	applicationName: String,
	metaGraph: MetaGraph,
	private val eventBus: EventBus = BaseModule.eventBus
) : JFrame(), GraphViewerView {

	private val controller = GraphViewerController()

	private val executionToolbar = createExecutionToolbar()
	private val menuBar = GraphViewerMenuBar(controller)

	private val graphNavigationView = GraphNavigationViewSwing(
		controller = controller.graphNavigationViewController,
		drawingView = controller.drawingView,
		viewManager = DrawViewModule.viewManager,
		contextBorderColor = null,
		allowCloseInHeader = false)

	private val closeRequestHandler: (CloseViewRequest) -> Unit = { handle(it) }

	init {
		controller.view = this
		buildUI()

		eventBus.register(CloseViewRequest::class, closeRequestHandler)

		addWindowListener(object : WindowAdapter() {
			override fun windowClosing(e: WindowEvent?) {
				controller.dispose()
			}
		})
		pack()
		setLocationRelativeTo(Frame.getFrames()[0])

		title = "$applicationName - ${metaGraph.graph.model!!.name.value}"
		jMenuBar = GraphViewerMenuBar(controller)
		SwingUtilities.invokeLater {
			// Must be set after JFrame has been realized to calculate default zoom factor
			controller.setMetaGraph(metaGraph)
		}

		isVisible = true
	}

	override fun dispose() {
		eventBus.unregister(closeRequestHandler)
		executionToolbar.dispose()
		menuBar.dispose()
	}

	private fun buildUI() {
		graphNavigationView.preferredSize = Dimension(1000, 800)
		layout = BorderLayout()
		add(executionToolbar, BorderLayout.NORTH)
		add(graphNavigationView, BorderLayout.CENTER)
	}

	private fun createExecutionToolbar(): ExecutionToolbarSwing =
		ExecutionToolbarSwing(
			controller.applicationContextHolder.scheduler,
			controller.applicationContextHolder.systemSpeed,
			controller,
			controller.toggleApplicationModeAction,
			controller.singleStepModeAction,
			controller.pauseOrResumeAction)

	private fun handle(event: CloseViewRequest) {
		if (event.view === controller.drawingView) {
			super.dispose()
			controller.dispose()
		}
	}
}