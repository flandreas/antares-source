package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.invocation.BusyHandler
import ch.scorpion.jabbah.base.invocation.InvocationHandler
import ch.scorpion.jabbah.draw.view.AbstractViewAction
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.draw.view.ViewManager
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.element.GraphElementCollector
import ch.scorpion.jabbah.graph.view.GraphView
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.awt.Frame
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.*

/** Provides in-depth information about the currently open [Graph].*/
class GraphInfoAction(
	viewManager: ViewManager = DrawViewModule.viewManager
) : AbstractViewAction("graph.action.info", viewManager = viewManager) {

	override fun execute(event: ActionEvent) {
		val graphView = (viewManager.activeView as DrawingView<*>).drawing as GraphView<*>
		//GraphElementCollector().collect(graphView.graph!!)
		GraphInfoPanel.showAsDialog(graphView.graph!!)
	}
}

/** Displays the result of a [GraphElementCollector] run for the specified [Graph].*/
class GraphInfoPanel(graph: Graph, private val closeHandler: () -> Unit) : JPanel() {

	companion object {
		fun showAsDialog(graph: Graph, parent: Frame = Frame.getFrames()[0]) {
			val dialog = JDialog(parent, true)

			BusyHandler.register(dialog, null)
			dialog.addWindowListener(object : WindowAdapter() {
				override fun windowClosed(e: WindowEvent?) {
					BusyHandler.deregister(dialog)
				}
			})

			dialog.title = Translations.getString("graph.action.info.name")
			dialog.contentPane.add(GraphInfoPanel(graph) { dialog.dispose()})
			dialog.pack()
			dialog.setLocationRelativeTo(parent)
			dialog.isVisible = true
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

		val buttonPanel = JPanel(FlowLayout(FlowLayout.LEFT))
		buttonPanel.add(JButton(CloseAction()))
		add(buttonPanel, BorderLayout.SOUTH)
	}

	private inner class CloseAction : AbstractAction(Translations.getString("file.action.close.name")) {
		override fun actionPerformed(e: java.awt.event.ActionEvent?) {
			closeHandler.invoke()
		}
	}
}