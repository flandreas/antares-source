package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.invocation.InvocationHandler
import ch.scorpion.jabbah.draw.view.AbstractViewAction
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.draw.view.ContentViewManager
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.element.GraphElementCollector
import ch.scorpion.jabbah.graph.view.GraphView
import java.awt.BorderLayout
import java.awt.Frame
import javax.swing.*

/** Provides in-depth statistical information about the currently open [Graph].*/
class GraphStatisticsAction(
	viewManager: ContentViewManager = DrawViewModule.viewManager
) : AbstractViewAction("graph.action.statistics", viewManager = viewManager) {

	override fun execute(event: ActionEvent) {
		val graphView = (viewManager.activeView!!.view as DrawingView<*>).drawing as GraphView
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

	private val deepTextArea = buildTextArea()
	private val flatTextArea = buildTextArea()

	init {
		buildUI()
		deepTextArea.text = Translations.getString("graph.action.statistics.calculating")
		InvocationHandler.invoke {
			val result = GraphElementCollector().collect(graph)
			deepTextArea.text = result.deep
			flatTextArea.text = result.flat
		}
	}

	private fun buildUI() {

		layout = BorderLayout()

		val tabPane = JTabbedPane()
		tabPane.add(Translations.getString("graph.action.statistics.deep"), buildPage(deepTextArea))
		tabPane.add(Translations.getString("graph.action.statistics.flat"), buildPage(flatTextArea))

		add(tabPane, BorderLayout.CENTER)
	}

	private fun buildTextArea(): JTextArea {
		val textArea = JTextArea();

		textArea.columns = 30
		textArea.rows = 30
		textArea.isEditable = false

		return textArea
	}

	private fun buildPage(textArea: JTextArea): JComponent {
		val panel = JPanel(BorderLayout())

		val scrollPane = JScrollPane(textArea)
		scrollPane.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
		scrollPane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
		panel.add(scrollPane, BorderLayout.CENTER)

		return panel
	}
}