package ch.scorpion.jabbah.graph.ui.graphviewer

import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.graph.GraphApplicationContextHolder
import ch.scorpion.jabbah.graph.ui.GraphNavigationViewSwing
import ch.scorpion.jabbah.graph.view.GraphView
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Frame
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JFrame
import javax.swing.SwingUtilities

class SubGraphViewerViewSwing(
	applicationName: String,
	graphView: GraphView,
	applicationContextHolder: GraphApplicationContextHolder
) : JFrame(), SubGraphViewerView {

	private val controller = SubGraphViewerController(graphView, applicationContextHolder)

	private val graphNavigationView = GraphNavigationViewSwing(
		controller = controller.graphNavigationViewController,
		drawingView = controller.drawingView,
		viewManager = DrawViewModule.viewManager,
		reusable = false,
		allowCloseInHeader = false)

	init {
		controller.view = this
		buildUI()

		addWindowListener(object : WindowAdapter() {
			override fun windowClosing(e: WindowEvent?) {
				controller.dispose()
			}
		})
		pack()
		setLocationRelativeTo(Frame.getFrames()[0])

		title = if (StringUtils.isNotBlank(applicationName)) {
			"$applicationName - ${graphView.name}"
		} else {
				"${graphView.name}"
		}
		SwingUtilities.invokeLater {
			// Must be set after JFrame has been realized to calculate default zoom factor
			controller.setGraphView(graphView)
		}

		isVisible = true
	}

	private fun buildUI() {
		graphNavigationView.preferredSize = Dimension(1000, 800)
		layout = BorderLayout()
		add(graphNavigationView, BorderLayout.CENTER)
	}
}