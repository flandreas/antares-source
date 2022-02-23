package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.swing.DialogBuilder
import ch.scorpion.jabbah.base.ui.Clipboard
import ch.scorpion.jabbah.edit.auth.Operation
import ch.scorpion.jabbah.graph.library.AbstractContainerLibraryElementAction
import ch.scorpion.jabbah.graph.library.ContainerLibraryElement
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.library.LibraryService
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewController
import java.awt.BorderLayout
import java.awt.Frame
import javax.swing.*

class MetaGraphEmbedAction(
	controller: LibraryTreeViewController
) : AbstractContainerLibraryElementAction(
	actionBaseName = "graph.action.embed",
	operation = Operation.View,
	controller
) {
	override fun execute(event: ActionEvent) {
		MetaGraphEmbedPanel.showAsDialog(uuid = (selectedItem as ContainerLibraryElement).uuid)
	}
}

internal class MetaGraphEmbedPanel(
	private val uuid: UUID,
	private val closeHandler: () -> Unit,
	private val libraryService: LibraryService = LibraryModule.libraryService
) : JPanel() {

	companion object {
		fun showAsDialog(
			parent: Frame = Frame.getFrames()[0],
			uuid: UUID
		) {
			DialogBuilder<MetaGraphEmbedPanel>(parent)
				.title(Translations.getString("graph.action.embed.title"))
				.content { dialog -> MetaGraphEmbedPanel(uuid, closeHandler = { dialog.dispose()}) }
				.defaultButton { it.closeButton }
				.show()
		}
	}

	private val tabPane = JTabbedPane()
	private val closeAction = CloseAction()
	private val copyToClipboardAction = CopyToClipboardAction()
	private val closeButton = createButton(closeAction)
	private val textAreas = mutableListOf<JTextArea>()

	init {
		buildUI()
	}

	private fun buildUI() {
		layout = BorderLayout(10, 10)
		border = BorderFactory.createEmptyBorder(10, 10, 10, 10)

		addPage(
			Translations.getString("graph.action.embed.iframe.name"),
			libraryService.getEmbeddingIFrame(uuid))

		add(tabPane, BorderLayout.CENTER)

		val buttonPanel = JPanel()
		buttonPanel.layout = BoxLayout(buttonPanel, BoxLayout.LINE_AXIS)

		buttonPanel.add(createButton(copyToClipboardAction))
		buttonPanel.add(Box.createHorizontalGlue())
		buttonPanel.add(closeButton)
		add(buttonPanel, BorderLayout.SOUTH)
	}

	private fun addPage(title: String, value: String) {
		val textArea = JTextArea(10, 40)
		textArea.text = value
		textArea.isEditable = false

		val page = buildPage(textArea)
		tabPane.add(title, page)
		textAreas.add(textArea)
	}

	private fun buildPage(textArea: JTextArea): JComponent {
		val scrollPane = JScrollPane(textArea)
		scrollPane.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
		scrollPane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED

		return scrollPane
	}

	private fun copyToClipboard() {
		Clipboard.setStringContents(textAreas[tabPane.selectedIndex].text)
	}

	private fun createButton(action: Action): JButton =
		JButton(ActionWrapperSwing(action))

	private inner class CloseAction : AbstractAction("file.action.close") {
		override fun execute(event: ActionEvent) {
			closeHandler.invoke()
		}
	}

	private inner class CopyToClipboardAction : AbstractAction("base.action.copyToClipboard") {
		override fun execute(event: ActionEvent) {
			copyToClipboard()
		}
	}
}