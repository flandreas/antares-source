package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.invocation.InvocationHandler
import ch.scorpion.jabbah.draw.view.AbstractViewAction
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.draw.view.ViewManager
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.element.GraphElementCollector
import ch.scorpion.jabbah.graph.view.GraphView
import java.awt.BorderLayout
import java.awt.Frame
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextArea

/** Provides in-depth statistical information about the currently open [Graph].*/
class GraphStatisticsAction(
	viewManager: ViewManager = DrawViewModule.viewManager
) : AbstractViewAction("graph.action.statistics", viewManager = viewManager) {

	override fun execute(event: ActionEvent) {
		val graphView = (viewManager.activeView as DrawingView<*>).drawing as GraphView<*>
		GraphInfoPanel.showAsDialog(title = Translations.getString("graph.action.statistics.name"), graph = graphView.graph!!)
	}
}

/** Displays the result of a [GraphElementCollector] run for the specified [Graph].*/
class GraphInfoPanel(graph: Graph) : JPanel() {

	companion object {
		fun showAsDialog(
			parent: Frame = Frame.getFrames()[0],
			title: String,
			graph: Graph
		) {
			JOptionPane.showConfirmDialog(
				parent,
				GraphInfoPanel(graph),
				title,
				JOptionPane.DEFAULT_OPTION,
				JOptionPane.PLAIN_MESSAGE
			)
		}
	}

	private val textArea = JTextArea()

	init {
		buildUI()
		InvocationHandler.invoke {
			textArea.text = GraphElementCollector().collect(graph)
		}
	}

	private fun buildUI() {
		textArea.columns = 30
		textArea.rows = 30
		textArea.isEditable = false

		layout = BorderLayout()

		val scrollPane = JScrollPane(textArea)
		scrollPane.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
		scrollPane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
		add(scrollPane, BorderLayout.CENTER)
	}
}