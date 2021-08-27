package ch.scorpion.jabbah.graph.ui.graphviewer

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.CloseViewRequest
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.graph.ui.ExecutionToolbarBuilder
import ch.scorpion.jabbah.graph.ui.GraphNavigationViewSwing
import ch.scorpion.jabbah.graph.view.GraphView
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Frame
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JFrame
import javax.swing.SwingUtilities

class GraphViewerFrameSwing(
	applicationName: String,
	graphView: GraphView,
	private val eventBus: EventBus = BaseModule.eventBus
) : JFrame(), GraphViewerView {

	private val controller = GraphViewerController()

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

		title = "$applicationName - ${graphView.graph!!.name.value}"
		jMenuBar = GraphViewerMenuBarBuilder().build()
		SwingUtilities.invokeLater {
			// Must be set after JFrame has been realized to calculate default zoom factor
			controller.setGraphView(graphView)
		}

		isVisible = true
	}

	override fun dispose() {
		eventBus.unregister(closeRequestHandler)
	}

	override fun notifyAllResourcesLoaded() { }

	private fun buildUI() {
		graphNavigationView.preferredSize = Dimension(1000, 800)
		layout = BorderLayout()
		add(BorderLayout.NORTH, ExecutionToolbarBuilder(
			controller.applicationContextHolder.scheduler,
			controller.applicationContextHolder.systemSpeed,
			controller,
			controller.toggleApplicationModeAction,
			eventBus
		).build())
		add(BorderLayout.CENTER, graphNavigationView)
	}

	private fun handle(event: CloseViewRequest) {
		if (event.view === controller.drawingView) {
			super.dispose()
			controller.dispose()
		}
	}
}