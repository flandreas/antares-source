package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.Settings
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.swing.DialogBuilder
import ch.scorpion.jabbah.base.swing.EGBL
import ch.scorpion.jabbah.base.ui.Clipboard
import ch.scorpion.jabbah.base.ui.UIBasics
import ch.scorpion.jabbah.draw.style.Theme
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.edit.auth.Operation
import ch.scorpion.jabbah.graph.library.AbstractContainerLibraryElementAction
import ch.scorpion.jabbah.graph.library.ContainerLibraryElement
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.library.LibraryService
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewController
import java.awt.BorderLayout
import java.awt.Frame
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
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
	private val closeHandler: (MetaGraphEmbedPanel) -> Unit,
	private val libraryService: LibraryService = LibraryModule.libraryService
) : JPanel() {

	companion object {

		/** The name of the entry in [Settings] for the preferred [Theme] name.*/
		private const val SETTING_EMBED_THEME = "metaGraphEmbed.theme"

		fun showAsDialog(
			parent: Frame = Frame.getFrames()[0],
			uuid: UUID
		) {
			DialogBuilder<MetaGraphEmbedPanel>(parent)
				.title(Translations.getString("graph.action.embed.title"))
				.content { dialog -> MetaGraphEmbedPanel(uuid, closeHandler = {
					it.dispose()
					dialog.dispose() })
				}
				.defaultButton { it.closeButton }
				.show()
		}
	}

	private val tabPane = JTabbedPane()
	private val closeAction = CloseAction()
	private val copyToClipboardAction = CopyToClipboardAction()
	private val closeButton = createButton(closeAction)
	private val pages = mutableListOf<Page>()
	private val themeComboBox = JComboBox<Theme>()

	private class Page(
		val title: String,
		val view: JComponent,
		val textArea: JTextArea,
		val contentProvider: () -> String
	)

	init {
		buildUI()
	}

	fun dispose() {
		BaseModule.settings.set(SETTING_EMBED_THEME, (themeComboBox.selectedItem as Theme).name)
	}

	private fun buildUI() {
		layout = BorderLayout(10, 10)
		border = UIBasics.createDialogBorder()

		add(buildParametersPanel(), BorderLayout.NORTH)

		pages.add(
			createPage(Translations.getString("graph.action.embed.iframe.name")) {
				libraryService.getEmbeddingIFrame(uuid, URLEncoder.encode((themeComboBox.selectedItem as Theme).name, StandardCharsets.UTF_8))
			})

		pages.forEach {
			tabPane.add(it.title, it.view)
		}

		add(tabPane, BorderLayout.CENTER)
		add(buildButtonsPanel(), BorderLayout.SOUTH)

		updatePages()
	}

	private fun createPage(title: String, contentProvider: () -> String): Page {
		val textArea = JTextArea(15, 60)
		textArea.text = ""
		textArea.isEditable = false
		return Page(title, buildPageView(textArea), textArea, contentProvider)
	}

	private fun buildPageView(textArea: JTextArea): JComponent {
		val scrollPane = JScrollPane(textArea)
		scrollPane.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
		scrollPane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED

		return scrollPane
	}

	private fun updatePages() {
		pages.forEach { it.textArea.text = it.contentProvider() }
	}

	private fun buildParametersPanel(): JPanel {
		var row = 0
		val inset = 5
		val panel = JPanel(EGBL.getLayout())

		Themes.allThemes().forEach { themeComboBox.addItem(it) }
		themeComboBox.selectedItem = Themes.get(BaseModule.settings.getString(SETTING_EMBED_THEME, Themes.allThemes().next().name))
		themeComboBox.addActionListener { updatePages() }

		EGBL.add(
			panel,
			JLabel("${Translations.getString("draw.preference.theme.name")}:"),
			0, row,
			1, 1,
			0.0, 0.0,
			EGBL.WEST,
			EGBL.NONE,
			0, inset, 0, 0)

		EGBL.add(
			panel,
			themeComboBox,
			1, row++,
			EGBL.REMAINDER, 1,
			0.0, 0.0,
			EGBL.WEST,
			EGBL.NONE,
			0, 10, 0, inset
		)

		// Filler
		EGBL.add(
			panel,
			JPanel(),
			2, row,
			EGBL.REMAINDER, EGBL.REMAINDER,
			1.0, 1.0,
			EGBL.NORTHWEST,
			EGBL.BOTH)

		return panel
	}

	private fun buildButtonsPanel(): JPanel {
		val buttonPanel = JPanel()
		buttonPanel.layout = BoxLayout(buttonPanel, BoxLayout.LINE_AXIS)

		buttonPanel.add(createButton(copyToClipboardAction))
		buttonPanel.add(Box.createHorizontalStrut(UIBasics.BUTTON_GAP))
		buttonPanel.add(Box.createHorizontalGlue())
		buttonPanel.add(closeButton)

		return buttonPanel
	}

	private fun copyToClipboard() {
		Clipboard.setStringContents(pages[tabPane.selectedIndex].textArea.text)
	}

	private fun createButton(action: Action): JButton =
		JButton(ActionWrapperSwing(action))

	private inner class CloseAction : AbstractAction("file.action.close") {
		override fun execute(event: ActionEvent) {
			closeHandler.invoke(this@MetaGraphEmbedPanel)
		}
	}

	private inner class CopyToClipboardAction : AbstractAction("base.action.copyToClipboard") {
		override fun execute(event: ActionEvent) {
			copyToClipboard()
		}
	}
}