package io.antarescircuit.jabbah.graph.ui

import io.antarescircuit.jabbah.base.*
import io.antarescircuit.jabbah.base.AbstractAction
import io.antarescircuit.jabbah.base.Action
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.swing.DialogBuilder
import io.antarescircuit.jabbah.base.swing.EGBL
import io.antarescircuit.jabbah.base.ui.Clipboard
import io.antarescircuit.jabbah.base.ui.UIBasics
import io.antarescircuit.jabbah.draw.style.Theme
import io.antarescircuit.jabbah.draw.style.Themes
import io.antarescircuit.jabbah.edit.auth.Operation
import io.antarescircuit.jabbah.graph.MetaGraph
import io.antarescircuit.jabbah.graph.library.AbstractContainerLibraryElementAction
import io.antarescircuit.jabbah.graph.library.ContainerLibraryElement
import io.antarescircuit.jabbah.graph.library.LibraryModule
import io.antarescircuit.jabbah.graph.library.LibraryService.Companion.PROP_VIEWER_JS_URL
import io.antarescircuit.jabbah.graph.ui.library.LibraryTreeViewController
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
	override val opensDialog: Boolean get() = true

	override fun execute(event: ActionEvent) {
		MetaGraphEmbedPanel.showAsDialog(name, uuid = (selectedItem as ContainerLibraryElement).uuid)
	}
}

internal class MetaGraphEmbedPanel(
	private val uuid: UUID,
	private val closeHandler: (MetaGraphEmbedPanel) -> Unit
) : JPanel() {

	companion object {

		/** The name of the entry in [Settings] for the preferred [Theme] name.*/
		private const val SETTING_EMBED_THEME = "metaGraphEmbed.theme"

		fun showAsDialog(
			title: String,
			parent: Frame = Frame.getFrames()[0],
			uuid: UUID
		) {
			DialogBuilder<MetaGraphEmbedPanel>(parent)
				.title(title)
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
				getEmbeddingIFrame(uuid, URLEncoder.encode((themeComboBox.selectedItem as Theme).name, StandardCharsets.UTF_8))
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

	/**
	 * Returns the code snippet to embed the [MetaGraph] with [UUID] as an HTML <iframe>.
	 * @param uuid the [UUID] of the [MetaGraph] to be embedded
	 * @param themeName the URL-encoded name of the [Theme] in which the [MetaGraph] is rendered
	 */
	private fun getEmbeddingIFrame(uuid: UUID, themeName: String): String {
		val metaGraph = LibraryModule.libraryHolder.getMetaGraph(uuid)
		val library = LibraryModule.libraryHolder.getContainingLibrary(uuid)!!
		val src = StringBuilder(BaseModule.properties.getString(PROP_VIEWER_JS_URL))
			.append("?")
			.append("library=${library.uuid.id}")
			.append("&circuit=${uuid.id}")
			.append("&theme=$themeName")
			.toString()

		return """
			|<iframe
			|   style="border:1px solid gray;"
			|   title="${metaGraph.name}"
			|   width="500px"
			|   height="500px"
			|   src="$src">
			|</iframe>
		""".trimMargin()
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