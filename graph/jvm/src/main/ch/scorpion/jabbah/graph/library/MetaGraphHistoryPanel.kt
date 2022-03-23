package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.invocation.InvocationHandler
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.swing.DialogBuilder
import ch.scorpion.jabbah.draw.view.CanvasJvm
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.auth.Operation
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.module.GraphModuleJvm
import ch.scorpion.jabbah.graph.ui.GraphDataViewController
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewController
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.graph.GraphViewImpl
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.Frame
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import javax.swing.*

class ShowMetaGraphHistoryAction(
	private val graphDataViewController: GraphDataViewController,
	controller: LibraryTreeViewController
) : AbstractContainerLibraryElementAction("graph.history.action", Operation.Change, controller) {

	override fun execute(event: ActionEvent) {
		MetaGraphHistoryPanel.showAsDialog(Frame.getFrames()[0], graphDataViewController, selectedItem as ContainerLibraryElement)
	}

	override fun calculateEnabledness(): Boolean =
		super.calculateEnabledness() && BaseModule.properties.getBoolean(FileMetaGraphHistoryService.PREF_META_GRAPH_HISTORY)
}

/**
 * Shows the save history of a [MetaGraph] and allows the user to restore a historized version.
 */
class MetaGraphHistoryPanel(
	private val graphDataViewController: GraphDataViewController,
	private val element: ContainerLibraryElement,
	private val historyService: FileMetaGraphHistoryService = GraphModuleJvm.metaGraphHistoryService,
	private val closeHandler: () -> Unit
) : JPanel() {

	companion object {
		private val LOG by logger(MetaGraphHistoryPanel::class)

		fun showAsDialog(
			parent: Frame,
			graphDataViewController: GraphDataViewController,
			element: ContainerLibraryElement
		) {
			DialogBuilder<MetaGraphHistoryPanel>(parent)
				.content { dialog -> MetaGraphHistoryPanel(graphDataViewController, element, closeHandler = { dialog.dispose() }) }
				.title(Translations.getString("graph.history.dialog.title"))
				.minimiumSize(Dimension(500, 300))
				.defaultButton { it.closeButton }
				.show()
		}
	}

	private val closeAction = CloseAction()
	private val restoreAction = RestoreAction()
	private val closeButton = JButton(ActionWrapperSwing(closeAction))

	private val historyList = JList(loadEntries())

	private val preview = CanvasJvm(EditModule.drawingViewFactory.create(GraphViewImpl() as Drawing<ch.scorpion.jabbah.edit.Component>, null, displayGlobalMessages = false))

	init {
		buildUI()

		restoreAction.enabled = false
		historyList.addListSelectionListener {
			restoreAction.enabled = historyList.selectedIndex > 0
			InvocationHandler.invoke {
				updatePreview(historyList.selectedValue)
			}
		}
	}

	private fun buildUI() {
		layout = BorderLayout(0, 10)
		border = BorderFactory.createEmptyBorder(10, 10, 10, 10)

		historyList.cellRenderer = HistoryRenderer()

		val scrollPane = JScrollPane(historyList)
		scrollPane.preferredSize = Dimension(200, 300)
		add(scrollPane, BorderLayout.WEST)

		preview.preferredSize = Dimension(300, 300)
		(preview.view as DrawingView<*>).showGrid = false
		add(preview, BorderLayout.CENTER)

		val buttonPanel = JPanel()
		buttonPanel.layout = BoxLayout(buttonPanel, BoxLayout.LINE_AXIS)
		buttonPanel.add(JButton(ActionWrapperSwing(restoreAction)))
		buttonPanel.add(Box.createHorizontalGlue())
		buttonPanel.add(Box.createHorizontalStrut(5))
		buttonPanel.add(closeButton)
		add(buttonPanel, BorderLayout.SOUTH)
	}

	private fun loadEntries(): ListModel<MetaGraphHistory> =
		DefaultListModel<MetaGraphHistory>().also { model ->
			historyService.getHistory(element.uuid).forEach { model.addElement(it) }
		}

	private fun restore(history: MetaGraphHistory) {
		val metaGraph = historyService.getMetaGraph(element.library!!, element.uuid, history)
		LOG.debug("Restoring historized version of ${metaGraph.uuid.id}")

		element.updateStorable(metaGraph)
		element.library!!.libraryService.updateContainerLibraryElement(element.library!!, element)
		graphDataViewController.openAsSavable(element, Translations.getString("graph.history.action.restore.name"))
	}

	private fun updatePreview(history: MetaGraphHistory) {
		val metaGraph = historyService.getMetaGraph(element.library!!, element.uuid, history)
		(preview.view as DrawingView<GraphView>).setDrawing(metaGraph.graph.graphView)
	}

	private inner class CloseAction : AbstractAction("file.action.close") {
		override fun execute(event: ActionEvent) {
			closeHandler.invoke()
		}
	}

	private inner class RestoreAction : AbstractAction("graph.history.action.restore") {
		override fun execute(event: ActionEvent) {
			restore(historyList.selectedValue)
			closeHandler.invoke()
		}
	}

	private class HistoryRenderer : DefaultListCellRenderer() {

		private val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)

		override fun getListCellRendererComponent(
			list: JList<*>?,
			value: Any?,
			index: Int,
			isSelected: Boolean,
			cellHasFocus: Boolean
		): Component {
			val renderer = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus) as JLabel
			renderer.text = formatter.format((value as MetaGraphHistory).timestamp)
			return renderer
		}
	}
}